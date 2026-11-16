package co.edu.javeriana.as.personapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.domain.Study;

/** HU-033: persona con cero, uno y varios teléfonos, sin los de otras personas. */
class PersonPhonesUseCaseTest {

	private static final int SIN = 1;
	private static final int UNO = 2;
	private static final int VARIOS = 3;

	private final Map<Integer, Person> people = new TreeMap<>();
	private final Map<String, Phone> phones = new TreeMap<>();
	private PersonPhonesUseCase useCase;

	@BeforeEach
	void setUp() {
		for (int cc : new int[] { SIN, UNO, VARIOS })
			people.put(cc, person(cc));
		phone("3001", UNO);
		phone("3200", VARIOS);
		phone("0100", VARIOS);
		phone("3105", VARIOS);
		useCase = new PersonPhonesUseCase(new PersonPort(), new PhonePort());
	}

	@Test
	void sinTelefonosDevuelveListaVacia() throws Exception {
		Person result = useCase.findWithPhones(SIN);
		assertEquals(SIN, result.getIdentification());
		assertTrue(result.getPhoneNumbers().isEmpty());
	}

	@Test
	void conUnTelefonoSoloTraeEseTelefono() throws Exception {
		assertEquals(List.of("3001"), numbers(useCase.findWithPhones(UNO)));
	}

	@Test
	void conVariosTelefonosLosOrdenaPorNumeroYExcluyeLosAjenos() throws Exception {
		Person result = useCase.findWithPhones(VARIOS);
		assertEquals(List.of("0100", "3105", "3200"), numbers(result));
		assertTrue(result.getPhoneNumbers().stream().allMatch(p -> p.getOwner().getIdentification() == VARIOS));
	}

	@Test
	void noExponeLosEstudios() throws Exception {
		people.get(VARIOS).setStudies(new ArrayList<Study>());
		assertNull(useCase.findWithPhones(VARIOS).getStudies());
	}

	@Test
	void personaInexistenteEsErr03YCedulaInvalidaEsErr01() {
		assertThrows(NoExistException.class, () -> useCase.findWithPhones(99));
		assertThrows(InvalidDataException.class, () -> useCase.findWithPhones(null));
		assertThrows(InvalidDataException.class, () -> useCase.findWithPhones(0));
	}

	private static Person person(int cc) {
		Person p = new Person();
		p.setIdentification(cc);
		p.setFirstName("P" + cc);
		p.setLastName("HU033");
		p.setGender(Gender.OTHER);
		return p;
	}

	private void phone(String number, int owner) {
		phones.put(number, new Phone(number, "Op", person(owner)));
	}

	private static List<String> numbers(Person p) {
		return p.getPhoneNumbers().stream().map(Phone::getNumber).toList();
	}

	private class PersonPort implements PersonOutputPort {
		public Person save(Person person) { return people.put(person.getIdentification(), person); }
		public Boolean delete(Integer id) { return people.remove(id) != null; }
		public List<Person> find() { return new ArrayList<>(people.values()); }
		public Person findById(Integer id) { return people.get(id); }
		public Integer count() { return people.size(); }
		public List<Person> find(Integer page, Integer size) { return find(); }
	}

	private class PhonePort implements PhoneOutputPort {
		public Phone save(Phone phone) { return phones.put(phone.getNumber(), phone); }
		public Boolean delete(String number) { return phones.remove(number) != null; }
		public List<Phone> find() { return new ArrayList<>(phones.values()); }
		public List<Phone> find(Integer page, Integer size) { return find(); }
		public Phone findById(String number) { return phones.get(number); }
		public Integer count() { return phones.size(); }
		public List<Phone> findByOwner(Integer id) {
			return phones.values().stream().filter(p -> p.getOwner().getIdentification().equals(id)).toList();
		}
	}
}
