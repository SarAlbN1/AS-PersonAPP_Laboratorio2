package co.edu.javeriana.as.personapp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.model.request.PersonaRequest;

/**
 * Regresión de HU-006: fromAdapterToDomain devolvía una Person vacía y el POST
 * fallaba con NullPointerException en validateGenero de los mappers de salida.
 */
class PersonaMapperRestTest {

	private final PersonaMapperRest mapper = new PersonaMapperRest();

	@Test
	void transformaTodosLosCamposDeLaSolicitud() {
		PersonaRequest request = new PersonaRequest("999", "Prueba", "Mapper", "30", "M", "MARIA");

		Person person = mapper.fromAdapterToDomain(request);

		assertEquals(999, person.getIdentification());
		assertEquals("Prueba", person.getFirstName());
		assertEquals("Mapper", person.getLastName());
		assertEquals(30, person.getAge());
		assertEquals(Gender.MALE, person.getGender());
	}

	@Test
	void aceptaFemeninoEnFormatoCortoYLargo() {
		assertEquals(Gender.FEMALE, mapper.fromAdapterToDomain(request("F")).getGender());
		assertEquals(Gender.FEMALE, mapper.fromAdapterToDomain(request("female")).getGender());
		assertEquals(Gender.MALE, mapper.fromAdapterToDomain(request("MALE")).getGender());
	}

	@Test
	void edadVaciaQuedaSinValor() {
		PersonaRequest request = new PersonaRequest("999", "Prueba", "Mapper", "", "F", "MONGO");

		assertNull(mapper.fromAdapterToDomain(request).getAge());
	}

	@Test
	void rechazaSexoNoAdmitido() {
		assertThrows(IllegalArgumentException.class, () -> mapper.fromAdapterToDomain(request("O")));
		assertThrows(IllegalArgumentException.class, () -> mapper.fromAdapterToDomain(request(null)));
	}

	@Test
	void rechazaDniAusenteONoNumerico() {
		assertThrows(IllegalArgumentException.class,
				() -> mapper.fromAdapterToDomain(new PersonaRequest(null, "A", "B", "1", "M", "MARIA")));
		assertThrows(IllegalArgumentException.class,
				() -> mapper.fromAdapterToDomain(new PersonaRequest("abc", "A", "B", "1", "M", "MARIA")));
	}

	@Test
	void respuestaConservaLosCamposTransformados() {
		Person person = mapper.fromAdapterToDomain(request("F"));

		assertEquals("999", mapper.fromDomainToAdapterRestMongo(person).getDni());
		assertEquals("FEMALE", mapper.fromDomainToAdapterRestMongo(person).getSex());
		assertEquals("MongoDB", mapper.fromDomainToAdapterRestMongo(person).getDatabase());
	}

	private PersonaRequest request(String sex) {
		return new PersonaRequest("999", "Prueba", "Mapper", "30", sex, "MARIA");
	}
}
