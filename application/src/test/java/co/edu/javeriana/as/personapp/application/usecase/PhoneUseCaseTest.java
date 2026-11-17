package co.edu.javeriana.as.personapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;

/**
 * HU-019: las seis operaciones de Teléfono y las reglas C-02 a C-08 de HU-011,
 * sobre puertos en memoria que ordenan por clave como lo harán los adaptadores.
 */
class PhoneUseCaseTest {

	private static final int PEPE = 123456789;
	private static final int PEPA = 321654987;

	private InMemoryPhonePort phones;
	private InMemoryPersonPort people;
	private PhoneUseCase useCase;

	@BeforeEach
	void setUp() {
		phones = new InMemoryPhonePort();
		people = new InMemoryPersonPort();
		people.save(new Person(PEPE, "Pepe", "Perez", Gender.MALE));
		people.save(new Person(PEPA, "Pepa", "Juarez", Gender.FEMALE));
		useCase = new PhoneUseCase(phones, people);
	}

	@Test
	void creaYConsultaUnTelefonoConSuDueno() throws Exception {
		useCase.create(phone("3001234567", "  Claro  ", PEPE));

		Phone saved = useCase.findOne("3001234567");
		assertEquals("Claro", saved.getCompany());
		assertEquals(PEPE, saved.getOwner().getIdentification());
		assertEquals("Pepe", saved.getOwner().getFirstName());
	}

	@Test
	void conservaLosCerosIniciales() throws Exception {
		useCase.create(phone("0012345", "Movistar", PEPE));

		assertEquals("0012345", useCase.findOne("0012345").getNumber());
		assertThrows(NoExistException.class, () -> useCase.findOne("12345"));
	}

	@Test
	void rechazaAltaDuplicadaSinSobrescribir() throws Exception {
		useCase.create(phone("3001234567", "Claro", PEPE));

		assertThrows(DuplicateException.class, () -> useCase.create(phone("3001234567", "Tigo", PEPA)));
		Phone kept = useCase.findOne("3001234567");
		assertEquals("Claro", kept.getCompany());
		assertEquals(PEPE, kept.getOwner().getIdentification());
	}

	@Test
	void rechazaDuenoInexistente() {
		assertThrows(ReferenceNotFoundException.class, () -> useCase.create(phone("3001234567", "Claro", 999)));
		assertEquals(0, useCase.count());
	}

	@Test
	void rechazaDatosInvalidos() {
		assertThrows(InvalidDataException.class, () -> useCase.create(null));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone(null, "Claro", PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("", "Claro", PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("300-123", "Claro", PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("+573001234567", "Claro", PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("1".repeat(16), "Claro", PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("3001234567", null, PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("3001234567", "   ", PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("3001234567", "x".repeat(46), PEPE)));
		assertThrows(InvalidDataException.class, () -> useCase.create(phone("3001234567", "Claro", null)));
		assertEquals(0, useCase.count());
	}

	@Test
	void aceptaLosLimitesDelEsquema() throws Exception {
		useCase.create(phone("1".repeat(15), "x".repeat(45), PEPE));
		useCase.create(phone("7", "Tigo", PEPE));

		assertEquals(2, useCase.count());
	}

	@Test
	void editaOperadorYCambiaDeDuenoExistente() throws Exception {
		useCase.create(phone("3001234567", "Claro", PEPE));

		useCase.edit("3001234567", phone("3001234567", "Tigo", PEPA));

		Phone edited = useCase.findOne("3001234567");
		assertEquals("Tigo", edited.getCompany());
		assertEquals(PEPA, edited.getOwner().getIdentification());
	}

	@Test
	void editarRechazaClaveInexistenteClaveDistintaYDuenoInexistente() throws Exception {
		useCase.create(phone("3001234567", "Claro", PEPE));

		assertThrows(NoExistException.class, () -> useCase.edit("3109999999", phone("3109999999", "Tigo", PEPE)));
		assertThrows(KeyMismatchException.class, () -> useCase.edit("3001234567", phone("3109999999", "Tigo", PEPE)));
		assertThrows(ReferenceNotFoundException.class,
				() -> useCase.edit("3001234567", phone("3001234567", "Tigo", 999)));
		Phone kept = useCase.findOne("3001234567");
		assertEquals("Claro", kept.getCompany());
		assertEquals(PEPE, kept.getOwner().getIdentification());
		assertEquals(1, useCase.count());
	}

	@Test
	void eliminaSinRestriccionYRechazaClaveInexistente() throws Exception {
		useCase.create(phone("3001234567", "Claro", PEPE));

		assertTrue(useCase.drop("3001234567"));
		assertEquals(0, useCase.count());
		assertThrows(NoExistException.class, () -> useCase.findOne("3001234567"));
		assertThrows(NoExistException.class, () -> useCase.drop("3001234567"));
	}

	@Test
	void unaClaveConFormatoInvalidoEsErr01AlConsultarYEliminar() {
		for (String clave : new String[] { "abc", "300-123", "", "1".repeat(16) }) {
			assertThrows(InvalidDataException.class, () -> useCase.findOne(clave), clave);
			assertThrows(InvalidDataException.class, () -> useCase.drop(clave), clave);
		}
		assertThrows(InvalidDataException.class, () -> useCase.findOne(null));
	}

	@Test
	void listaTodosEnOrdenDeTexto() throws Exception {
		for (String number : new String[] { "9", "300", "1000", "0012" })
			useCase.create(phone(number, "Claro", PEPE));

		// Orden de texto (C-06): "1000" va antes que "300" y "9".
		assertEquals(List.of("0012", "1000", "300", "9"), numbers(useCase.findAll()));
	}

	@Test
	void listaParcialPorPaginasConOrdenEstable() throws Exception {
		for (int i = 1; i <= 5; i++)
			useCase.create(phone("30000000" + i, "Claro", PEPE));

		assertEquals(List.of("300000001", "300000002"), numbers(useCase.findAll(0, 2)));
		assertEquals(List.of("300000003", "300000004"), numbers(useCase.findAll(1, 2)));
		assertEquals(List.of("300000005"), numbers(useCase.findAll(2, 2)));
		assertTrue(useCase.findAll(3, 2).isEmpty());
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
		useCase.create(phone("3001234567", "Claro", PEPE));
		useCase.create(phone("3107654321", "Movistar", PEPE));
		useCase.create(phone("3209876543", "Tigo", PEPA));

		assertEquals(3, useCase.count());
	}

	private static Phone phone(String number, String company, Integer ownerCc) {
		// Los setters de Lombok rechazan null; un campo ausente queda sin asignar,
		// como cuando un mapper parte del constructor vacío.
		Phone phone = new Phone();
		if (number != null)
			phone.setNumber(number);
		if (company != null)
			phone.setCompany(company);
		if (ownerCc != null) {
			Person owner = new Person();
			owner.setIdentification(ownerCc);
			phone.setOwner(owner);
		}
		return phone;
	}

	private static List<String> numbers(List<Phone> list) {
		return list.stream().map(Phone::getNumber).collect(Collectors.toList());
	}

	private static class InMemoryPhonePort implements PhoneOutputPort {
		private final TreeMap<String, Phone> data = new TreeMap<>();

		@Override
		public Phone save(Phone phone) {
			data.put(phone.getNumber(), phone);
			return phone;
		}

		@Override
		public Boolean delete(String number) {
			return data.remove(number) != null;
		}

		@Override
		public List<Phone> find() {
			return new ArrayList<>(data.values());
		}

		@Override
		public List<Phone> find(Integer page, Integer size) {
			return data.values().stream().skip((long) page * size).limit(size).collect(Collectors.toList());
		}

		@Override
		public Phone findById(String number) {
			return data.get(number);
		}

		@Override
		public List<Phone> findByOwner(Integer identification) {
			return data.values().stream().filter(p -> p.getOwner().getIdentification().equals(identification))
					.collect(Collectors.toList());
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
	}
}
