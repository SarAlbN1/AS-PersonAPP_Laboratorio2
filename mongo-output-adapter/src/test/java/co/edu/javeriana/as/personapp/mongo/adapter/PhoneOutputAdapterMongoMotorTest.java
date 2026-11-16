package co.edu.javeriana.as.personapp.mongo.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import co.edu.javeriana.as.personapp.application.usecase.PhoneUseCase;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;

/**
 * HU-020: ejercita la persistencia de Teléfono contra un MongoDB real. MongoDB no
 * tiene FK, así que el dueño lo comprueba el caso de uso en el mismo motor (C-03).
 * Corre solo con -Dmotores=arriba:
 *
 * <pre>
 * docker compose -f compose.yaml -f compose.pruebas.yaml up -d mariadb mongo
 * ./mvnw -B -ntp -pl mongo-output-adapter -am -Dmotores=arriba test
 * </pre>
 *
 * Usa cédulas 99002x y números que empiezan por 099002 o 99002, y los borra al
 * terminar cada caso.
 */
@DataMongoTest
@TestPropertySource(properties = {
		"spring.mongodb.uri=mongodb://persona_db:persona_db@localhost:${PERSONAPP_MONGO_PORT:27017}/persona_db?authSource=admin",
		"spring.main.allow-circular-references=true" })
@EnabledIfSystemProperty(named = "motores", matches = "arriba")
class PhoneOutputAdapterMongoMotorTest {

	private static final int CC_UNO = 990021;
	private static final int CC_DOS = 990022;
	private static final int CC_AUSENTE = 990029;
	private static final String[] NUMEROS = { "0990020001", "990020001", "990020002", "99002009", "990020010",
			"0990020" };

	@Autowired
	private PhoneOutputAdapterMongo adaptador;

	@Autowired
	private PersonOutputAdapterMongo personas;

	@Autowired
	private MongoTemplate mongo;

	@BeforeEach
	void crearDuenos() {
		limpiar();
		for (int cc : new int[] { CC_UNO, CC_DOS }) {
			Person p = new Person();
			p.setIdentification(cc);
			p.setFirstName("Llama");
			p.setLastName("HU020");
			p.setGender(Gender.FEMALE);
			personas.save(p);
		}
	}

	@AfterEach
	void limpiar() {
		for (String numero : NUMEROS)
			adaptador.delete(numero);
		for (int cc : new int[] { CC_UNO, CC_DOS })
			personas.delete(cc);
	}

	private static Phone telefono(String numero, String operador, int cc) {
		Person dueno = new Person();
		dueno.setIdentification(cc);
		Phone phone = new Phone();
		phone.setNumber(numero);
		phone.setCompany(operador);
		phone.setOwner(dueno);
		return phone;
	}

	@Test
	void guardaYConsultaConSuDuenoYLosCerosIniciales() {
		Phone guardado = adaptador.save(telefono("0990020001", "Claro", CC_UNO));
		assertEquals("HU020", guardado.getOwner().getLastName());

		Phone leido = adaptador.findById("0990020001");
		assertNotNull(leido);
		assertEquals("0990020001", leido.getNumber());
		assertEquals("Claro", leido.getCompany());
		assertEquals(CC_UNO, leido.getOwner().getIdentification());
		assertEquals("Llama", leido.getOwner().getFirstName());
		assertNull(leido.getOwner().getPhoneNumbers(), "C-10: no se cargan los teléfonos del dueño");
		assertNull(adaptador.findById("990020001"), "sin el cero inicial es otro número");
	}

	@Test
	void consultarUnNumeroAusenteDevuelveNulo() {
		assertNull(adaptador.findById("990020099"));
	}

	@Test
	void elDuenoSeGuardaComoReferenciaALaCedula() {
		adaptador.save(telefono("990020001", "Claro", CC_UNO));
		Document crudo = mongo.findById("990020001", Document.class, "telefono");
		assertNotNull(crudo);
		assertEquals(CC_UNO, crudo.get("primaryDuenio"), "igual que el DML: la cédula, no un subdocumento");
	}

	@Test
	void editarCambiaOperadorYDueno() {
		adaptador.save(telefono("990020001", "Claro", CC_UNO));
		adaptador.save(telefono("990020001", "Tigo", CC_DOS));

		Phone leido = adaptador.findById("990020001");
		assertEquals("Tigo", leido.getCompany());
		assertEquals(CC_DOS, leido.getOwner().getIdentification());
	}

	@Test
	void listarYPaginarOrdenanPorNumeroComoTexto() {
		// Como texto, "990020010" va antes que "99002009"; como número sería al revés.
		for (String numero : new String[] { "99002009", "990020010", "0990020" })
			adaptador.save(telefono(numero, "Claro", CC_UNO));

		List<String> todos = adaptador.find().stream().map(Phone::getNumber).toList();
		assertEquals(todos.stream().sorted().toList(), todos, "C-06");
		assertTrue(todos.indexOf("0990020") < todos.indexOf("990020010"));
		assertTrue(todos.indexOf("990020010") < todos.indexOf("99002009"));

		int total = adaptador.count();
		List<String> recorridos = new ArrayList<>();
		for (int pagina = 0; pagina * 2 < total; pagina++)
			adaptador.find(pagina, 2).forEach(phone -> recorridos.add(phone.getNumber()));
		assertEquals(todos, recorridos, "C-07: las páginas recorren el mismo orden sin repetir");
		assertTrue(adaptador.find(10_000, 10).isEmpty());
	}

	@Test
	void contarYEliminarUsanElMotorSinTocarAlDueno() {
		int antes = adaptador.count();
		adaptador.save(telefono("990020001", "Claro", CC_UNO));
		adaptador.save(telefono("990020002", "Tigo", CC_UNO));
		assertEquals(antes + 2, adaptador.count());

		assertTrue(adaptador.delete("990020001"));
		assertNull(adaptador.findById("990020001"));
		assertNotNull(adaptador.findById("990020002"), "solo se borra el número indicado");
		assertEquals(antes + 1, adaptador.count());
		assertNotNull(personas.findById(CC_UNO), "C-05: borrar el teléfono no toca al dueño");
	}

	@Test
	void elCasoDeUsoCompruebaElDuenoYElDuplicadoEnElMotor() throws Exception {
		PhoneUseCase telefonos = new PhoneUseCase(adaptador, personas);
		assertThrows(ReferenceNotFoundException.class,
				() -> telefonos.create(telefono("990020001", "Claro", CC_AUSENTE)), "C-03: dueño inexistente");
		assertNull(adaptador.findById("990020001"), "sin FK, es el caso de uso el que evita la referencia rota");

		telefonos.create(telefono("990020001", "Claro", CC_UNO));
		assertThrows(DuplicateException.class, () -> telefonos.create(telefono("990020001", "Tigo", CC_DOS)),
				"C-02: no sobrescribe");
		assertEquals("Claro", adaptador.findById("990020001").getCompany());
	}
}
