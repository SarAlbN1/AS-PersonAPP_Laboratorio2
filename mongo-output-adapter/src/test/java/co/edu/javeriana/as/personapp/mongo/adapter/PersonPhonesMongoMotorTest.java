package co.edu.javeriana.as.personapp.mongo.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import co.edu.javeriana.as.personapp.application.usecase.PersonPhonesUseCase;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;

/**
 * HU-033: persona con sus teléfonos contra un MongoDB real. Corre solo con
 * -Dmotores=arriba y las bases de compose.pruebas.yaml. Usa cédulas 99003x y las borra
 * antes y después de cada caso.
 */
@DataMongoTest
@TestPropertySource(properties = {
		"spring.mongodb.uri=mongodb://persona_db:persona_db@localhost:${PERSONAPP_MONGO_PORT:27017}/persona_db?authSource=admin",
		"spring.main.allow-circular-references=true" })
@EnabledIfSystemProperty(named = "motores", matches = "arriba")
class PersonPhonesMongoMotorTest {

	private static final int SIN = 990031;
	private static final int UNO = 990032;
	private static final int VARIOS = 990033;
	private static final int AJENA = 990034;
	private static final List<String> NUMEROS = List.of("9900310", "9900320", "0990330", "9900331", "9900332", "9900340");

	@Autowired
	private PhoneOutputAdapterMongo telefonos;

	@Autowired
	private PersonOutputAdapterMongo personas;

	@BeforeEach
	void cargar() {
		limpiar();
		for (int cc : new int[] { SIN, UNO, VARIOS, AJENA }) {
			Person p = new Person();
			p.setIdentification(cc);
			p.setFirstName("Consulta");
			p.setLastName("HU033");
			p.setGender(Gender.FEMALE);
			personas.save(p);
		}
		telefonos.save(telefono("9900320", UNO));
		telefonos.save(telefono("9900332", VARIOS));
		telefonos.save(telefono("0990330", VARIOS));
		telefonos.save(telefono("9900331", VARIOS));
		telefonos.save(telefono("9900340", AJENA));
	}

	@AfterEach
	void limpiar() {
		NUMEROS.forEach(telefonos::delete);
		for (int cc : new int[] { SIN, UNO, VARIOS, AJENA })
			personas.delete(cc);
	}

	private static Phone telefono(String numero, int cc) {
		Person dueno = new Person();
		dueno.setIdentification(cc);
		return new Phone(numero, "Op", dueno);
	}

	private static List<String> numeros(Person persona) {
		return persona.getPhoneNumbers().stream().map(Phone::getNumber).toList();
	}

	@Test
	void elPuertoFiltraPorDuenoYOrdenaPorNumeroEnElMotor() {
		assertTrue(telefonos.findByOwner(SIN).isEmpty());
		assertEquals(List.of("9900320"), telefonos.findByOwner(UNO).stream().map(Phone::getNumber).toList());
		assertEquals(List.of("0990330", "9900331", "9900332"),
				telefonos.findByOwner(VARIOS).stream().map(Phone::getNumber).toList(),
				"orden de texto: el cero inicial va primero y se conserva");
		assertTrue(telefonos.findByOwner(VARIOS).stream().allMatch(t -> t.getOwner().getIdentification() == VARIOS));
	}

	@Test
	void elCasoDeUsoDevuelveLaPersonaConCeroUnoOVariosTelefonos() throws Exception {
		PersonPhonesUseCase consulta = new PersonPhonesUseCase(personas, telefonos);
		Person sin = consulta.findWithPhones(SIN);
		assertEquals("HU033", sin.getLastName());
		assertTrue(sin.getPhoneNumbers().isEmpty());
		assertEquals(List.of("9900320"), numeros(consulta.findWithPhones(UNO)));
		assertEquals(List.of("0990330", "9900331", "9900332"), numeros(consulta.findWithPhones(VARIOS)));
		assertEquals(List.of("9900340"), numeros(consulta.findWithPhones(AJENA)));
		assertThrows(NoExistException.class, () -> consulta.findWithPhones(990039));
	}
}
