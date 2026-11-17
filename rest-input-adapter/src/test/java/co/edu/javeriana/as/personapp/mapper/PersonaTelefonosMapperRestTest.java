package co.edu.javeriana.as.personapp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.model.response.PersonaTelefonosResponse;
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;

/** HU-033: la persona sale con sus teléfonos en el orden del caso de uso, o con lista vacía. */
class PersonaTelefonosMapperRestTest {

	private final PersonaTelefonosMapperRest mapper = new PersonaTelefonosMapperRest(new TelefonoMapperRest());

	private static Person persona(Integer age, List<Phone> phones) {
		Person person = new Person();
		person.setIdentification(123456789);
		person.setFirstName("Pepe");
		person.setLastName("Perez");
		person.setGender(Gender.MALE);
		if (age != null)
			person.setAge(age);
		if (phones != null)
			person.setPhoneNumbers(phones);
		return person;
	}

	private static Phone telefono(String number, Person owner) {
		Phone phone = new Phone();
		phone.setNumber(number);
		phone.setCompany("Claro");
		phone.setOwner(owner);
		return phone;
	}

	@Test
	void sinTelefonosDevuelveListaVacia() {
		PersonaTelefonosResponse r = mapper.fromDomainToAdapterRest(persona(30, List.of()));
		assertEquals("123456789", r.dni());
		assertEquals("Pepe", r.firstName());
		assertEquals("Perez", r.lastName());
		assertEquals("30", r.age());
		assertEquals("MALE", r.sex());
		assertTrue(r.phones().isEmpty());
	}

	@Test
	void variosTelefonosConservanElOrdenYLosCerosIniciales() {
		Person owner = new Person();
		owner.setIdentification(123456789);
		PersonaTelefonosResponse r = mapper.fromDomainToAdapterRest(
				persona(30, List.of(telefono("0012345", owner), telefono("3001234567", owner))));
		assertEquals(List.of(new TelefonoResponse("0012345", "Claro", 123456789),
				new TelefonoResponse("3001234567", "Claro", 123456789)), r.phones());
	}

	@Test
	void edadSinDatoSaleNulaYNoComoTexto() {
		PersonaTelefonosResponse r = mapper.fromDomainToAdapterRest(persona(null, null));
		assertNull(r.age());
		assertTrue(r.phones().isEmpty());
	}
}
