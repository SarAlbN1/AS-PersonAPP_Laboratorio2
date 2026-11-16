package co.edu.javeriana.as.personapp.mariadb.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import co.edu.javeriana.as.personapp.application.usecase.PersonPhonesUseCase;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;

/**
 * HU-033: persona con sus teléfonos contra un MariaDB real. Corre solo con
 * -Dmotores=arriba y las bases de compose.pruebas.yaml. Usa cédulas 99003x dentro de
 * una transacción que se revierte.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:mariadb://localhost:${PERSONAPP_MARIA_PORT:3307}/persona_db",
		"spring.datasource.username=persona_db",
		"spring.datasource.password=persona_db",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.main.allow-circular-references=true" })
@EnabledIfSystemProperty(named = "motores", matches = "arriba")
class PersonPhonesMariaMotorTest {

	private static final int SIN = 990031;
	private static final int UNO = 990032;
	private static final int VARIOS = 990033;
	private static final int AJENA = 990034;

	@Autowired
	private PhoneOutputAdapterMaria telefonos;

	@Autowired
	private PersonOutputAdapterMaria personas;

	@Autowired
	private TestEntityManager em;

	@BeforeEach
	void cargar() {
		for (int cc : new int[] { SIN, UNO, VARIOS, AJENA }) {
			Person p = new Person();
			p.setIdentification(cc);
			p.setFirstName("Consulta");
			p.setLastName("HU033");
			p.setGender(Gender.FEMALE);
			personas.save(p);
		}
		em.flush();
		em.clear();
		telefonos.save(telefono("9900320", UNO));
		telefonos.save(telefono("9900332", VARIOS));
		telefonos.save(telefono("0990330", VARIOS));
		telefonos.save(telefono("9900331", VARIOS));
		telefonos.save(telefono("9900340", AJENA));
		em.flush();
		em.clear();
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
