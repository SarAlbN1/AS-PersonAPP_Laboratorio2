package co.edu.javeriana.as.personapp.mariadb.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntityPK;
import co.edu.javeriana.as.personapp.mariadb.entity.PersonaEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.ProfesionEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.TelefonoEntity;

/**
 * Regresión de #47: con estudios o teléfonos, los mappers de MariaDB se llamaban
 * mutuamente sin corte y la lectura terminaba en StackOverflowError.
 */
class MapperRecursionMariaTest {

	private PersonaMapperMaria personaMapper;
	private ProfesionMapperMaria profesionMapper;
	private EstudiosMapperMaria estudiosMapper;
	private TelefonoMapperMaria telefonoMapper;

	@BeforeEach
	void setUp() throws Exception {
		personaMapper = new PersonaMapperMaria();
		profesionMapper = new ProfesionMapperMaria();
		estudiosMapper = new EstudiosMapperMaria();
		telefonoMapper = new TelefonoMapperMaria();
		inject(personaMapper, "estudiosMapperMaria", estudiosMapper);
		inject(personaMapper, "telefonoMapperMaria", telefonoMapper);
		inject(profesionMapper, "estudiosMapperMaria", estudiosMapper);
		inject(estudiosMapper, "personaMapperMaria", personaMapper);
		inject(estudiosMapper, "profesionMapperMaria", profesionMapper);
		inject(telefonoMapper, "personaMapperMaria", personaMapper);
	}

	@Test
	void personaSinEstudiosNiTelefonos() {
		Person person = personaMapper.fromAdapterToDomain(persona(123456789));
		assertEquals(123456789, person.getIdentification());
		assertTrue(person.getStudies().isEmpty());
		assertTrue(person.getPhoneNumbers().isEmpty());
	}

	@Test
	void personaConUnEstudioYUnTelefono() {
		PersonaEntity persona = persona(123456789);
		ProfesionEntity profesion = profesion(9001);
		estudio(persona, profesion, LocalDate.of(2020, 5, 15), "Javeriana");
		telefono("3009990047", persona);

		Person person = personaMapper.fromAdapterToDomain(persona);

		assertEquals(1, person.getStudies().size());
		Study study = person.getStudies().get(0);
		assertNotNull(study, "el estudio no puede llegar como null");
		assertEquals(LocalDate.of(2020, 5, 15), study.getGraduationDate());
		assertEquals("Javeriana", study.getUniversityName());
		assertEquals(123456789, study.getPerson().getIdentification());
		assertEquals("Pepe", study.getPerson().getFirstName());
		assertNull(study.getPerson().getStudies(), "la persona del estudio no recarga sus estudios");
		assertEquals(9001, study.getProfession().getIdentification());
		assertNull(study.getProfession().getStudies(), "la profesión del estudio no recarga sus estudios");

		assertEquals(1, person.getPhoneNumbers().size());
		Phone phone = person.getPhoneNumbers().get(0);
		assertEquals("3009990047", phone.getNumber());
		assertEquals(123456789, phone.getOwner().getIdentification());
		assertNull(phone.getOwner().getPhoneNumbers(), "el dueño del teléfono no recarga sus teléfonos");
	}

	@Test
	void personaConVariosEstudiosYTelefonos() {
		PersonaEntity persona = persona(123456789);
		estudio(persona, profesion(9001), LocalDate.of(2020, 5, 15), "Javeriana");
		estudio(persona, profesion(9002), null, "Andes");
		estudio(persona, profesion(9003), LocalDate.of(2024, 1, 1), "Nacional");
		telefono("3001", persona);
		telefono("3002", persona);

		Person person = personaMapper.fromAdapterToDomain(persona);

		assertEquals(List.of(9001, 9002, 9003),
				person.getStudies().stream().map(s -> s.getProfession().getIdentification()).toList());
		assertNull(person.getStudies().get(1).getGraduationDate());
		assertEquals(List.of("3001", "3002"), person.getPhoneNumbers().stream().map(Phone::getNumber).toList());
	}

	@Test
	void fechaDeEstudioLeidaPorJpaComoSqlDate() {
		PersonaEntity persona = persona(123456789);
		EstudiosEntity estudio = estudio(persona, profesion(9001), null, "Javeriana");
		estudio.setFecha(java.sql.Date.valueOf(LocalDate.of(2020, 5, 15)));

		Person person = personaMapper.fromAdapterToDomain(persona);

		assertEquals(LocalDate.of(2020, 5, 15), person.getStudies().get(0).getGraduationDate());
	}

	@Test
	void profesionConEstudios() {
		PersonaEntity persona = persona(123456789);
		ProfesionEntity profesion = profesion(9001);
		estudio(persona, profesion, LocalDate.of(2020, 5, 15), "Javeriana");

		Profession profession = profesionMapper.fromAdapterToDomain(profesion);

		assertEquals(1, profession.getStudies().size());
		assertEquals(123456789, profession.getStudies().get(0).getPerson().getIdentification());
		assertNull(profession.getStudies().get(0).getProfession().getStudies());
	}

	@Test
	void telefonoConDuenioQueTieneMasTelefonos() {
		PersonaEntity persona = persona(123456789);
		TelefonoEntity telefono = telefono("3001", persona);
		telefono("3002", persona);

		Phone phone = telefonoMapper.fromAdapterToDomain(telefono);

		assertEquals("Pepe", phone.getOwner().getFirstName());
		assertNull(phone.getOwner().getPhoneNumbers());
	}

	@Test
	void escrituraDeTelefonoConGrafoCiclicoUsaSoloLaClaveDelDuenio() {
		Person owner = new Person(123456789, "Pepe", "Perez", Gender.MALE);
		Phone phone = new Phone("3001", "Claro", owner);
		owner.setPhoneNumbers(new ArrayList<>(List.of(phone)));

		TelefonoEntity entity = telefonoMapper.fromDomainToAdapter(phone);

		assertEquals(123456789, entity.getDuenio().getCc());
		assertNull(entity.getDuenio().getTelefonos());
	}

	@Test
	void escrituraDePersonaConTelefonosYEstudiosTermina() {
		Person person = new Person(123456789, "Pepe", "Perez", Gender.MALE);
		Profession profession = new Profession(9001, "Prueba");
		Study study = new Study(person, profession);
		profession.setStudies(new ArrayList<>(List.of(study)));
		person.setStudies(new ArrayList<>(List.of(study)));
		person.setPhoneNumbers(new ArrayList<>(List.of(new Phone("3001", "Claro", person))));

		PersonaEntity entity = personaMapper.fromDomainToAdapter(person);

		assertEquals(1, entity.getTelefonos().size());
		assertEquals(123456789, entity.getTelefonos().get(0).getDuenio().getCc());
		assertEquals(9001, entity.getEstudios().get(0).getEstudiosPK().getIdProf());
	}

	private static PersonaEntity persona(int cc) {
		PersonaEntity persona = new PersonaEntity(cc, "Pepe", "Perez", 'M');
		persona.setEdad(30);
		persona.setEstudios(new ArrayList<>());
		persona.setTelefonos(new ArrayList<>());
		return persona;
	}

	private static ProfesionEntity profesion(int id) {
		ProfesionEntity profesion = new ProfesionEntity();
		profesion.setId(id);
		profesion.setNom("Profesion " + id);
		profesion.setEstudios(new ArrayList<>());
		return profesion;
	}

	private static EstudiosEntity estudio(PersonaEntity persona, ProfesionEntity profesion, LocalDate fecha,
			String univer) {
		EstudiosEntityPK pk = new EstudiosEntityPK();
		pk.setIdProf(profesion.getId());
		pk.setCcPer(persona.getCc());
		EstudiosEntity estudio = new EstudiosEntity(pk);
		estudio.setFecha(fecha == null ? null : Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		estudio.setUniver(univer);
		estudio.setPersona(persona);
		estudio.setProfesion(profesion);
		persona.getEstudios().add(estudio);
		profesion.getEstudios().add(estudio);
		return estudio;
	}

	private static TelefonoEntity telefono(String num, PersonaEntity duenio) {
		TelefonoEntity telefono = new TelefonoEntity(num);
		telefono.setOper("Claro");
		telefono.setDuenio(duenio);
		duenio.getTelefonos().add(telefono);
		return telefono;
	}

	private static void inject(Object target, String field, Object value) throws Exception {
		Field f = target.getClass().getDeclaredField(field);
		f.setAccessible(true);
		f.set(target, value);
	}
}
