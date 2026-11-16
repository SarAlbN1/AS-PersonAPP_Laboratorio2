package co.edu.javeriana.as.personapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;

/**
 * HU-022: las seis operaciones de Estudios y las reglas C-02 a C-08 de HU-011 con la
 * clave compuesta (profesión, persona), sobre puertos en memoria que ordenan por
 * id de profesión y luego por cc, como lo harán los adaptadores.
 */
class StudyUseCaseTest {

	private static final int PEPE = 123456789;
	private static final int PEPA = 321654987;
	private static final int INGENIERIA = 1;
	private static final int MEDICINA = 2;
	private static final LocalDate GRADO = LocalDate.of(2015, 12, 1);

	private InMemoryStudyPort studies;
	private InMemoryPersonPort people;
	private InMemoryProfessionPort professions;
	private StudyUseCase useCase;

	@BeforeEach
	void setUp() {
		studies = new InMemoryStudyPort();
		people = new InMemoryPersonPort();
		professions = new InMemoryProfessionPort();
		people.save(new Person(PEPE, "Pepe", "Perez", Gender.MALE));
		people.save(new Person(PEPA, "Pepa", "Juarez", Gender.FEMALE));
		professions.save(new Profession(INGENIERIA, "Ingeniería"));
		professions.save(new Profession(MEDICINA, "Medicina"));
		useCase = new StudyUseCase(studies, people, professions);
	}

	@Test
	void creaYConsultaUnEstudioPorSuClaveCompleta() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, GRADO, "  Javeriana  "));

		Study saved = useCase.findOne(INGENIERIA, PEPE);
		assertEquals("Javeriana", saved.getUniversityName());
		assertEquals(GRADO, saved.getGraduationDate());
		assertEquals("Pepe", saved.getPerson().getFirstName());
		assertEquals("Ingeniería", saved.getProfession().getName());
		assertThrows(NoExistException.class, () -> useCase.findOne(PEPE, INGENIERIA));
	}

	@Test
	void fechaYUniversidadSonOpcionales() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, null, null));
		useCase.create(study(MEDICINA, PEPE, null, "   "));

		assertNull(useCase.findOne(INGENIERIA, PEPE).getGraduationDate());
		assertNull(useCase.findOne(MEDICINA, PEPE).getUniversityName());
		assertEquals(2, useCase.count());
	}

	@Test
	void rechazaParDuplicadoSinSobrescribir() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, GRADO, "Javeriana"));

		assertThrows(DuplicateException.class, () -> useCase.create(study(INGENIERIA, PEPE, null, "Andes")));
		assertEquals("Javeriana", useCase.findOne(INGENIERIA, PEPE).getUniversityName());
	}

	@Test
	void permiteLaMismaPersonaConOtraProfesionYLaMismaProfesionConOtraPersona() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, GRADO, "Javeriana"));
		useCase.create(study(MEDICINA, PEPE, GRADO, "Rosario"));
		useCase.create(study(INGENIERIA, PEPA, GRADO, "Andes"));

		assertEquals(3, useCase.count());
	}

	@Test
	void rechazaReferenciasInexistentes() {
		assertThrows(ReferenceNotFoundException.class, () -> useCase.create(study(INGENIERIA, 999, GRADO, null)));
		assertThrows(ReferenceNotFoundException.class, () -> useCase.create(study(99, PEPE, GRADO, null)));
		assertEquals(0, useCase.count());
	}

	@Test
	void rechazaDatosInvalidos() {
		assertThrows(InvalidDataException.class, () -> useCase.create(null));
		assertThrows(InvalidDataException.class, () -> useCase.create(study(null, PEPE, GRADO, null)));
		assertThrows(InvalidDataException.class, () -> useCase.create(study(INGENIERIA, null, GRADO, null)));
		assertThrows(InvalidDataException.class, () -> useCase.create(study(0, PEPE, GRADO, null)));
		assertThrows(InvalidDataException.class, () -> useCase.create(study(INGENIERIA, -5, GRADO, null)));
		assertThrows(InvalidDataException.class,
				() -> useCase.create(study(INGENIERIA, PEPE, GRADO, "x".repeat(51))));
		assertEquals(0, useCase.count());
	}

	@Test
	void aceptaUniversidadDeCincuentaCaracteres() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, GRADO, "x".repeat(50)));

		assertEquals(1, useCase.count());
	}

	@Test
	void editarSoloCambiaFechaYUniversidad() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, GRADO, "Javeriana"));

		LocalDate nueva = LocalDate.of(2018, 6, 15);
		useCase.edit(INGENIERIA, PEPE, study(INGENIERIA, PEPE, nueva, "Andes"));

		Study edited = useCase.findOne(INGENIERIA, PEPE);
		assertEquals(nueva, edited.getGraduationDate());
		assertEquals("Andes", edited.getUniversityName());
		assertEquals("Pepe", edited.getPerson().getFirstName());
		assertEquals(1, useCase.count());
	}

	@Test
	void editarRechazaClaveInexistenteYClaveDistinta() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, GRADO, "Javeriana"));

		assertThrows(NoExistException.class, () -> useCase.edit(MEDICINA, PEPE, study(MEDICINA, PEPE, GRADO, "Andes")));
		assertThrows(KeyMismatchException.class,
				() -> useCase.edit(INGENIERIA, PEPE, study(MEDICINA, PEPE, GRADO, "Andes")));
		assertThrows(KeyMismatchException.class,
				() -> useCase.edit(INGENIERIA, PEPE, study(INGENIERIA, PEPA, GRADO, "Andes")));
		assertEquals("Javeriana", useCase.findOne(INGENIERIA, PEPE).getUniversityName());
		assertEquals(1, useCase.count());
	}

	@Test
	void eliminaSinRestriccionYRechazaClaveInexistente() throws Exception {
		useCase.create(study(INGENIERIA, PEPE, GRADO, "Javeriana"));
		useCase.create(study(MEDICINA, PEPE, GRADO, "Rosario"));

		assertTrue(useCase.drop(INGENIERIA, PEPE));
		assertEquals(1, useCase.count());
		assertThrows(NoExistException.class, () -> useCase.findOne(INGENIERIA, PEPE));
		assertThrows(NoExistException.class, () -> useCase.drop(INGENIERIA, PEPE));
		assertEquals("Rosario", useCase.findOne(MEDICINA, PEPE).getUniversityName());
	}

	@Test
	void listaTodosPorProfesionYLuegoPorPersona() throws Exception {
		useCase.create(study(MEDICINA, PEPE, GRADO, null));
		useCase.create(study(INGENIERIA, PEPA, GRADO, null));
		useCase.create(study(INGENIERIA, PEPE, GRADO, null));
		useCase.create(study(MEDICINA, PEPA, GRADO, null));

		assertEquals(List.of(key(INGENIERIA, PEPE), key(INGENIERIA, PEPA), key(MEDICINA, PEPE), key(MEDICINA, PEPA)),
				keys(useCase.findAll()));
	}

	@Test
	void listaParcialPorPaginasConOrdenEstable() throws Exception {
		useCase.create(study(MEDICINA, PEPE, GRADO, null));
		useCase.create(study(INGENIERIA, PEPA, GRADO, null));
		useCase.create(study(INGENIERIA, PEPE, GRADO, null));

		assertEquals(List.of(key(INGENIERIA, PEPE), key(INGENIERIA, PEPA)), keys(useCase.findAll(0, 2)));
		assertEquals(List.of(key(MEDICINA, PEPE)), keys(useCase.findAll(1, 2)));
		assertTrue(useCase.findAll(2, 2).isEmpty());
	}

	@Test
	void listaParcialRechazaParametrosInvalidos() throws Exception {
		assertThrows(InvalidDataException.class, () -> useCase.findAll(-1, 10));
		assertThrows(InvalidDataException.class, () -> useCase.findAll(0, 0));
		assertThrows(InvalidDataException.class, () -> useCase.findAll(0, 101));
		assertThrows(InvalidDataException.class, () -> useCase.findAll(null, 10));
		assertThrows(InvalidDataException.class, () -> useCase.findAll(0, null));
		assertTrue(useCase.findAll(0, 100).isEmpty());
	}

	@Test
	void cuentaCeroEnColeccionVaciaYElTotalDelMotor() throws Exception {
		assertEquals(0, useCase.count());
		useCase.create(study(INGENIERIA, PEPE, GRADO, null));
		useCase.create(study(MEDICINA, PEPA, GRADO, null));

		assertEquals(2, useCase.count());
	}

	private static Study study(Integer professionId, Integer personId, LocalDate date, String university) {
		// Los setters de Lombok rechazan null; un campo ausente queda sin asignar,
		// como cuando un mapper parte del constructor vacío.
		Study study = new Study();
		if (professionId != null) {
			Profession profession = new Profession();
			profession.setIdentification(professionId);
			study.setProfession(profession);
		}
		if (personId != null) {
			Person person = new Person();
			person.setIdentification(personId);
			study.setPerson(person);
		}
		study.setGraduationDate(date);
		study.setUniversityName(university);
		return study;
	}

	private static String key(int professionId, int personId) {
		return professionId + "/" + personId;
	}

	private static List<String> keys(List<Study> list) {
		return list.stream()
				.map(s -> key(s.getProfession().getIdentification(), s.getPerson().getIdentification()))
				.collect(Collectors.toList());
	}

	private static class InMemoryStudyPort implements StudyOutputPort {
		private static final Comparator<int[]> BY_KEY = Comparator.<int[]>comparingInt(k -> k[0])
				.thenComparingInt(k -> k[1]);
		private final TreeMap<int[], Study> data = new TreeMap<>(BY_KEY);

		private static int[] k(Integer professionId, Integer personId) {
			return new int[] { professionId, personId };
		}

		@Override
		public Study save(Study study) {
			data.put(k(study.getProfession().getIdentification(), study.getPerson().getIdentification()), study);
			return study;
		}

		@Override
		public Boolean delete(Integer professionId, Integer personId) {
			return data.remove(k(professionId, personId)) != null;
		}

		@Override
		public List<Study> find() {
			return new ArrayList<>(data.values());
		}

		@Override
		public List<Study> find(Integer page, Integer size) {
			return data.values().stream().skip((long) page * size).limit(size).collect(Collectors.toList());
		}

		@Override
		public Study findById(Integer professionId, Integer personId) {
			return data.get(k(professionId, personId));
		}

		@Override
		public Integer count() {
			return data.size();
		}
	}

	private static class InMemoryPersonPort implements PersonOutputPort {
		private final TreeMap<Integer, Person> data = new TreeMap<>();

		@Override
		public Person save(Person person) {
			data.put(person.getIdentification(), person);
			return person;
		}

		@Override
		public Boolean delete(Integer identification) {
			return data.remove(identification) != null;
		}

		@Override
		public List<Person> find() {
			return new ArrayList<>(data.values());
		}

		@Override
		public Person findById(Integer identification) {
			return data.get(identification);
		}

		@Override
		public Integer count() {
			return data.size();
		}

		@Override
		public List<Person> find(Integer page, Integer size) {
			return data.values().stream().skip((long) page * size).limit(size).collect(Collectors.toList());
		}

		@Override
		public Boolean hasPhones(Integer identification) {
			return false;
		}

		@Override
		public Boolean hasStudies(Integer identification) {
			return false;
		}
	}

	private static class InMemoryProfessionPort implements ProfessionOutputPort {
		private final TreeMap<Integer, Profession> data = new TreeMap<>();

		@Override
		public Profession save(Profession profession) {
			data.put(profession.getIdentification(), profession);
			return profession;
		}

		@Override
		public Boolean delete(Integer identification) {
			return data.remove(identification) != null;
		}

		@Override
		public List<Profession> find() {
			return new ArrayList<>(data.values());
		}

		@Override
		public List<Profession> find(Integer page, Integer size) {
			return data.values().stream().skip((long) page * size).limit(size).collect(Collectors.toList());
		}

		@Override
		public Profession findById(Integer identification) {
			return data.get(identification);
		}

		@Override
		public Integer count() {
			return data.size();
		}

		@Override
		public Boolean hasStudies(Integer identification) {
			return false;
		}
	}
}
