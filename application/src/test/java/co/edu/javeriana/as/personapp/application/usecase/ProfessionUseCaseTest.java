package co.edu.javeriana.as.personapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Profession;

/**
 * HU-016: las seis operaciones de Profesión y las reglas C-02 a C-08 de HU-011,
 * sobre un puerto en memoria que ordena por id como lo harán los adaptadores.
 */
class ProfessionUseCaseTest {

	private InMemoryProfessionPort port;
	private ProfessionUseCase useCase;

	@BeforeEach
	void setUp() {
		port = new InMemoryProfessionPort();
		useCase = new ProfessionUseCase(port);
	}

	@Test
	void creaYConsultaUnaProfesion() throws Exception {
		useCase.create(new Profession(1, "  Ingeniería  ", "Sistemas", null));

		Profession saved = useCase.findOne(1);
		assertEquals("Ingeniería", saved.getName());
		assertEquals("Sistemas", saved.getDescription());
	}

	@Test
	void rechazaAltaDuplicadaSinSobrescribir() throws Exception {
		useCase.create(new Profession(1, "Ingeniería", null, null));

		assertThrows(DuplicateException.class, () -> useCase.create(new Profession(1, "Otra", null, null)));
		assertEquals("Ingeniería", useCase.findOne(1).getName());
	}

	@Test
	void rechazaDatosInvalidos() {
		assertThrows(InvalidDataException.class, () -> useCase.create(null));
		assertThrows(InvalidDataException.class, () -> useCase.create(profession(null, "Ingeniería")));
		assertThrows(InvalidDataException.class, () -> useCase.create(profession(0, "Ingeniería")));
		assertThrows(InvalidDataException.class, () -> useCase.create(profession(1, null)));
		assertThrows(InvalidDataException.class, () -> useCase.create(profession(1, "   ")));
		assertThrows(InvalidDataException.class, () -> useCase.create(profession(1, "x".repeat(91))));
		assertEquals(0, useCase.count());
	}

	@Test
	void aceptaNombreDeNoventaCaracteres() throws Exception {
		useCase.create(profession(1, "x".repeat(90)));

		assertEquals(1, useCase.count());
	}

	@Test
	void editaUnaProfesionExistente() throws Exception {
		useCase.create(new Profession(1, "Ingeniería", null, null));

		useCase.edit(1, new Profession(1, "Medicina", "Salud", null));

		assertEquals("Medicina", useCase.findOne(1).getName());
		assertEquals("Salud", useCase.findOne(1).getDescription());
	}

	@Test
	void editarRechazaClaveInexistenteOClaveDistinta() throws Exception {
		useCase.create(profession(1, "Ingeniería"));

		assertThrows(NoExistException.class, () -> useCase.edit(2, profession(2, "Medicina")));
		assertThrows(KeyMismatchException.class, () -> useCase.edit(1, profession(2, "Medicina")));
		assertEquals(1, useCase.count());
	}

	@Test
	void eliminaUnaProfesionSinEstudios() throws Exception {
		useCase.create(profession(1, "Ingeniería"));

		assertTrue(useCase.drop(1));
		assertEquals(0, useCase.count());
		assertThrows(NoExistException.class, () -> useCase.findOne(1));
	}

	@Test
	void eliminarEsRestrictivoYRechazaClaveInexistente() throws Exception {
		useCase.create(profession(1, "Ingeniería"));
		port.studiesOf.add(1);

		assertThrows(DependentRecordsException.class, () -> useCase.drop(1));
		assertEquals(1, useCase.count());
		assertThrows(NoExistException.class, () -> useCase.drop(99));
	}

	@Test
	void listaTodasEnOrdenDeClave() throws Exception {
		for (int id : new int[] { 3, 1, 2 })
			useCase.create(profession(id, "P" + id));

		assertEquals(List.of(1, 2, 3), ids(useCase.findAll()));
	}

	@Test
	void listaParcialPorPaginasConOrdenEstable() throws Exception {
		for (int id = 1; id <= 5; id++)
			useCase.create(profession(id, "P" + id));

		assertEquals(List.of(1, 2), ids(useCase.findPage(0, 2)));
		assertEquals(List.of(3, 4), ids(useCase.findPage(1, 2)));
		assertEquals(List.of(5), ids(useCase.findPage(2, 2)));
		assertTrue(useCase.findPage(3, 2).isEmpty());
	}

	@Test
	void listaParcialRechazaParametrosInvalidos() throws Exception {
		assertThrows(InvalidDataException.class, () -> useCase.findPage(-1, 10));
		assertThrows(InvalidDataException.class, () -> useCase.findPage(0, 0));
		assertThrows(InvalidDataException.class, () -> useCase.findPage(0, 101));
		assertTrue(useCase.findPage(0, 100).isEmpty());
	}

	@Test
	void cuentaCeroEnColeccionVaciaYElTotalDelMotor() throws Exception {
		assertEquals(0, useCase.count());
		useCase.create(profession(1, "Ingeniería"));
		useCase.create(profession(2, "Medicina"));

		assertEquals(2, useCase.count());
	}

	private static Profession profession(Integer id, String name) {
		// Los setters de Lombok rechazan null; un campo ausente queda sin asignar,
		// como cuando un mapper parte del constructor vacío.
		Profession profession = new Profession();
		if (id != null)
			profession.setIdentification(id);
		if (name != null)
			profession.setName(name);
		return profession;
	}

	private static List<Integer> ids(List<Profession> professions) {
		return professions.stream().map(Profession::getIdentification).collect(Collectors.toList());
	}

	private static class InMemoryProfessionPort implements ProfessionOutputPort {
		private final TreeMap<Integer, Profession> data = new TreeMap<>();
		private final Set<Integer> studiesOf = new HashSet<>();

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
		public List<Profession> find(int page, int size) {
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
			return studiesOf.contains(identification);
		}
	}
}
