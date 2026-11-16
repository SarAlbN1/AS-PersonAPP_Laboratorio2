package co.edu.javeriana.as.personapp.mariadb.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.context.TestPropertySource;

import co.edu.javeriana.as.personapp.application.usecase.PhoneUseCase;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mariadb.repository.TelefonoRepositoryMaria;

/**
 * HU-020: ejercita la persistencia de Teléfono contra un MariaDB real, con la FK
 * `duenio` del DDL. Corre solo con -Dmotores=arriba y las bases publicadas:
 *
 * <pre>
 * docker compose -f compose.yaml -f compose.pruebas.yaml up -d mariadb mongo
 * ./mvnw -B -ntp -pl maria-output-adapter -am -Dmotores=arriba test
 * </pre>
 *
 * Usa cédulas 99002x y números que empiezan por 099002 o 99002, dentro de una
 * transacción que se revierte. Cada lectura vacía antes el contexto de persistencia,
 * así que va al motor.
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
class PhoneOutputAdapterMariaMotorTest {

	private static final int CC_UNO = 990021;
	private static final int CC_DOS = 990022;
	private static final int CC_AUSENTE = 990029;

	@Autowired
	private PhoneOutputAdapterMaria adaptador;

	@Autowired
	private PersonOutputAdapterMaria personas;

	@Autowired
	private TelefonoRepositoryMaria repositorio;

	@Autowired
	private TestEntityManager em;

	@BeforeEach
	void crearDuenos() {
		for (int cc : new int[] { CC_UNO, CC_DOS }) {
			Person p = new Person();
			p.setIdentification(cc);
			p.setFirstName("Llama");
			p.setLastName("HU020");
			p.setGender(Gender.FEMALE);
			personas.save(p);
		}
		sincronizar();
	}

	private void sincronizar() {
		em.flush();
		em.clear();
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
		sincronizar();

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
	void editarCambiaOperadorYDueno() {
		adaptador.save(telefono("990020001", "Claro", CC_UNO));
		sincronizar();
		adaptador.save(telefono("990020001", "Tigo", CC_DOS));
		sincronizar();

		Phone leido = adaptador.findById("990020001");
		assertEquals("Tigo", leido.getCompany());
		assertEquals(CC_DOS, leido.getOwner().getIdentification());
	}

	@Test
	void listarYPaginarOrdenanPorNumeroComoTexto() {
		// Como texto, "990020010" va antes que "99002009"; como número sería al revés.
		for (String numero : new String[] { "99002009", "990020010", "0990020" })
			adaptador.save(telefono(numero, "Claro", CC_UNO));
		sincronizar();

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
		sincronizar();
		assertEquals(antes + 2, adaptador.count());

		assertTrue(adaptador.delete("990020001"));
		sincronizar();
		assertNull(adaptador.findById("990020001"));
		assertNotNull(adaptador.findById("990020002"), "solo se borra el número indicado");
		assertEquals(antes + 1, adaptador.count());
		assertNotNull(personas.findById(CC_UNO), "C-05: borrar el teléfono no toca al dueño");
	}

	@Test
	void contarDevuelveCeroConLaTablaVacia() {
		// Dentro de la transacción de la prueba, que se revierte al terminar.
		repositorio.deleteAllInBatch();
		sincronizar();
		assertEquals(0, adaptador.count());
		assertTrue(adaptador.find().isEmpty());
	}

	@Test
	void elAdaptadorRechazaUnDuenoInexistenteSinGuardar() {
		// Hibernate resuelve el dueño por su cédula al guardar y no lo encuentra.
		assertThrows(ObjectRetrievalFailureException.class,
				() -> adaptador.save(telefono("990020001", "Claro", CC_AUSENTE)));
	}

	@Test
	void laFkDelMotorRechazaUnDuenoInexistente() {
		// Un INSERT directo, sin pasar por JPA: la FK `duenio` del DDL es el respaldo.
		PersistenceException e = assertThrows(PersistenceException.class,
				() -> em.getEntityManager()
						.createNativeQuery("INSERT INTO telefono (num, oper, duenio) VALUES ('990020001', 'Claro', ?)")
						.setParameter(1, CC_AUSENTE).executeUpdate());
		assertTrue(cadena(e).contains("foreign key"), cadena(e));
	}

	private static String cadena(Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (Throwable t = e; t != null; t = t.getCause())
			sb.append(t.getMessage()).append(" | ");
		return sb.toString().toLowerCase();
	}

	@Test
	void elCasoDeUsoCompruebaElDuenoYElDuplicadoEnElMotor() throws Exception {
		PhoneUseCase telefonos = new PhoneUseCase(adaptador, personas);
		assertThrows(ReferenceNotFoundException.class,
				() -> telefonos.create(telefono("990020001", "Claro", CC_AUSENTE)), "C-03: dueño inexistente");
		assertNull(adaptador.findById("990020001"));

		telefonos.create(telefono("990020001", "Claro", CC_UNO));
		sincronizar();
		assertThrows(DuplicateException.class, () -> telefonos.create(telefono("990020001", "Tigo", CC_DOS)),
				"C-02: no sobrescribe");
		sincronizar();
		assertEquals("Claro", adaptador.findById("990020001").getCompany());
	}
}
