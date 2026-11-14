package co.edu.javeriana.as.personapp.mongo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.document.PersonaDocument;
import co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument;
import co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument;

/**
 * Regresión de #47 en MongoDB: con estudios o teléfonos, los mappers se llamaban
 * mutuamente sin corte y la lectura terminaba en StackOverflowError.
 */
class MapperRecursionMongoTest {

	private PersonaMapperMongo personaMapper;
	private ProfesionMapperMongo profesionMapper;
	private EstudiosMapperMongo estudiosMapper;
	private TelefonoMapperMongo telefonoMapper;

	@BeforeEach
	void setUp() throws Exception {
		personaMapper = new PersonaMapperMongo();
		profesionMapper = new ProfesionMapperMongo();
		estudiosMapper = new EstudiosMapperMongo();
		telefonoMapper = new TelefonoMapperMongo();
		inject(personaMapper, "estudiosMapperMongo", estudiosMapper);
		inject(personaMapper, "telefonoMapperMongo", telefonoMapper);
		inject(profesionMapper, "estudiosMapperMongo", estudiosMapper);
		inject(estudiosMapper, "personaMapperMongo", personaMapper);
		inject(estudiosMapper, "profesionMapperMongo", profesionMapper);
		inject(telefonoMapper, "personaMapperMongo", personaMapper);
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
		PersonaDocument persona = persona(123456789);
		ProfesionDocument profesion = profesion(9001);
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
		PersonaDocument persona = persona(123456789);
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
	void profesionConEstudios() {
		PersonaDocument persona = persona(123456789);
		ProfesionDocument profesion = profesion(9001);
		estudio(persona, profesion, LocalDate.of(2020, 5, 15), "Javeriana");

		Profession profession = profesionMapper.fromAdapterToDomain(profesion);

		assertEquals(1, profession.getStudies().size());
		assertEquals(123456789, profession.getStudies().get(0).getPerson().getIdentification());
		assertNull(profession.getStudies().get(0).getProfession().getStudies());
	}

	@Test
	void telefonoConDuenioQueTieneMasTelefonos() {
		PersonaDocument persona = persona(123456789);
		TelefonoDocument telefono = telefono("3001", persona);
		telefono("3002", persona);

		Phone phone = telefonoMapper.fromAdapterToDomain(telefono);

		assertEquals("Pepe", phone.getOwner().getFirstName());
		assertNull(phone.getOwner().getPhoneNumbers());
	}

	@Test
	void escrituraDeEstudioConGrafoCiclicoUsaSoloLasClaves() {
		Person person = new Person(123456789, "Pepe", "Perez", Gender.MALE);
		Profession profession = new Profession(9001, "Prueba");
		Study study = new Study(person, profession);
		person.setStudies(new ArrayList<>(List.of(study)));
		profession.setStudies(new ArrayList<>(List.of(study)));

		EstudiosDocument document = estudiosMapper.fromDomainToAdapter(study);

		assertEquals("123456789-9001", document.getId());
		assertEquals(123456789, document.getPrimaryPersona().getId());
		assertNull(document.getPrimaryPersona().getEstudios());
		assertEquals(9001, document.getPrimaryProfesion().getId());
		assertNull(document.getPrimaryProfesion().getEstudios());
	}

	@Test
	void escrituraDeTelefonoConGrafoCiclicoUsaSoloLaClaveDelDuenio() {
		Person owner = new Person(123456789, "Pepe", "Perez", Gender.MALE);
		Phone phone = new Phone("3001", "Claro", owner);
		owner.setPhoneNumbers(new ArrayList<>(List.of(phone)));

		TelefonoDocument document = telefonoMapper.fromDomainToAdapter(phone);

		assertEquals(123456789, document.getPrimaryDuenio().getId());
		assertNull(document.getPrimaryDuenio().getTelefonos());
	}

	private static PersonaDocument persona(int cc) {
		PersonaDocument persona = new PersonaDocument();
		persona.setId(cc);
		persona.setNombre("Pepe");
		persona.setApellido("Perez");
		persona.setGenero("M");
		persona.setEdad(30);
		persona.setEstudios(new ArrayList<>());
		persona.setTelefonos(new ArrayList<>());
		return persona;
	}

	private static ProfesionDocument profesion(int id) {
		ProfesionDocument profesion = new ProfesionDocument();
		profesion.setId(id);
		profesion.setNom("Profesion " + id);
		profesion.setEstudios(new ArrayList<>());
		return profesion;
	}

	private static void estudio(PersonaDocument persona, ProfesionDocument profesion, LocalDate fecha,
			String univer) {
		EstudiosDocument estudio = new EstudiosDocument();
		estudio.setId(persona.getId() + "-" + profesion.getId());
		estudio.setPrimaryPersona(persona);
		estudio.setPrimaryProfesion(profesion);
		estudio.setFecha(fecha);
		estudio.setUniver(univer);
		persona.getEstudios().add(estudio);
		profesion.getEstudios().add(estudio);
	}

	private static TelefonoDocument telefono(String num, PersonaDocument duenio) {
		TelefonoDocument telefono = new TelefonoDocument();
		telefono.setId(num);
		telefono.setOper("Claro");
		telefono.setPrimaryDuenio(duenio);
		duenio.getTelefonos().add(telefono);
		return telefono;
	}

	private static void inject(Object target, String field, Object value) throws Exception {
		Field f = target.getClass().getDeclaredField(field);
		f.setAccessible(true);
		f.set(target, value);
	}
}
