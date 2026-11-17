package co.edu.javeriana.as.personapp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.model.request.TelefonoRequest;
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;

/** HU-021: el mapper conserva el número como texto y no falla con campos ausentes. */
class TelefonoMapperRestTest {

	private final TelefonoMapperRest mapper = new TelefonoMapperRest();

	@Test
	void copiaLosCamposYConservaLosCerosIniciales() {
		Phone phone = mapper.fromAdapterToDomain(new TelefonoRequest("0012345", "Claro", 123456789));
		assertEquals("0012345", phone.getNumber());
		assertEquals("Claro", phone.getCompany());
		assertEquals(123456789, phone.getOwner().getIdentification());
	}

	@Test
	void unCampoAusenteQuedaSinAsignarParaQueElCasoDeUsoDeErr01() {
		Phone phone = mapper.fromAdapterToDomain(new TelefonoRequest(null, null, null));
		assertNull(phone.getNumber());
		assertNull(phone.getCompany());
		assertNull(phone.getOwner());
	}

	@Test
	void laRespuestaLlevaLaCedulaDelDueno() {
		Person owner = new Person();
		owner.setIdentification(321654987);
		owner.setFirstName("Pepa");
		Phone phone = new Phone();
		phone.setNumber("3209876543");
		phone.setCompany("Tigo");
		phone.setOwner(owner);
		assertEquals(new TelefonoResponse("3209876543", "Tigo", 321654987), mapper.fromDomainToAdapterRest(phone));
	}
}
