package co.edu.javeriana.as.personapp.mongo.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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

import co.edu.javeriana.as.personapp.application.usecase.ProfessionUseCase;
import co.edu.javeriana.as.personapp.application.usecase.StudyUseCase;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;

/**
 * HU-023: ejercita la persistencia de Estudios contra un MongoDB real. MongoDB no
 * tiene FK, así que las referencias y los dependientes los comprueba el caso de uso
 * en el mismo motor (C-03, C-05). Corre solo con -Dmotores=arriba:
 *
 * <pre>
 * docker compose -f compose.yaml -f compose.pruebas.yaml up -d mariadb mongo
 * ./mvnw -B -ntp -pl mongo-output-adapter -am -Dmotores=arriba test
 * </pre>
 *
 * Usa cédulas 99001x y profesiones 9901x y las borra al terminar cada caso.
 */
@DataMongoTest
@TestPropertySource(properties = {
		"spring.mongodb.uri=mongodb://persona_db:persona_db@localhost:${PERSONAPP_MONGO_PORT:27017}/persona_db?authSource=admin",
		"spring.main.allow-circular-references=true" })
@EnabledIfSystemProperty(named = "motores", matches = "arriba")
class StudyOutputAdapterMongoMotorTest {

	private static final int CC_UNO = 990011;
	private static final int CC_DOS = 990012;
	private static final int PROF_UNO = 99011;
	private static final int PROF_DOS = 99012;
	private static final LocalDate GRADO = LocalDate.of(2015, 12, 1);

	@Autowired
	private StudyOutputAdapterMongo adaptador;

	@Autowired
	private PersonOutputAdapterMongo personas;

	@Autowired
	private ProfessionOutputAdapterMongo profesiones;

	@Autowired
	private MongoTemplate mongo;

	@BeforeEach
	void crearReferencias() {
		limpiar();
		for (int cc : new int[] { CC_UNO, CC_DOS }) {
			Person p = new Person();
			p.setIdentification(cc);
			p.setFirstName("Estudia");
			p.setLastName("HU023");
			p.setGender(Gender.MALE);
			personas.save(p);
		}
		for (int id : new int[] { PROF_UNO, PROF_DOS })
			profesiones.save(new Profession(id, "Prof " + id, null, null));
	}

	@AfterEach
	void limpiar() {
		for (int cc : new int[] { CC_UNO, CC_DOS })
			for (int id : new int[] { PROF_UNO, PROF_DOS })
				adaptador.delete(id, cc);
		for (int cc : new int[] { CC_UNO, CC_DOS })
			personas.delete(cc);
		for (int id : new int[] { PROF_UNO, PROF_DOS })
			profesiones.delete(id);
	}

	private static Study estudio(int prof, int cc, LocalDate fecha, String univer) {
		Person p = new Person();
		p.setIdentification(cc);
		Profession f = new Profession();
		f.setIdentification(prof);
		return new Study(p, f, fecha, univer);
	}

	@Test
	void guardaYConsultaPorLaClaveCompletaConSusReferencias() {
		Study guardado = adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO, "Javeriana"));
		assertEquals("HU023", guardado.getPerson().getLastName());

		Study leido = adaptador.findById(PROF_UNO, CC_UNO);
		assertNotNull(leido);
		assertEquals(CC_UNO, leido.getPerson().getIdentification());
		assertEquals("Prof " + PROF_UNO, leido.getProfession().getName());
		assertEquals(GRADO, leido.getGraduationDate());
		assertEquals("Javeriana", leido.getUniversityName());
		assertNull(adaptador.findById(PROF_DOS, CC_UNO), "la otra mitad de la clave no basta");

		Document crudo = mongo.getCollection("estudios").find(new Document("_id", CC_UNO + "-" + PROF_UNO)).first();
		assertNotNull(crudo, "el _id es ccPersona-idProfesion (C-10)");
		assertEquals(CC_UNO, crudo.get("primaryPersona"));
		assertEquals(PROF_UNO, crudo.get("primaryProfesion"));
	}

	@Test
	void laFechaSeGuardaEnUtcSinDependerDeLaZonaDeLaJvm() {
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO, null));
		Document crudo = mongo.getCollection("estudios").find(new Document("_id", CC_UNO + "-" + PROF_UNO)).first();
		assertEquals(Date.from(Instant.parse("2015-12-01T00:00:00Z")), crudo.getDate("fecha"));
		assertFalse(crudo.containsKey("univer") && crudo.get("univer") != null, "univer ausente no se guarda como texto");
		assertNull(adaptador.findById(PROF_UNO, CC_UNO).getUniversityName());
	}

	@Test
	void editarCambiaFechaYUniversidadSinCambiarLaClave() {
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO, "Javeriana"));
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO.plusYears(1), "Andes"));
		Study leido = adaptador.findById(PROF_UNO, CC_UNO);
		assertEquals(GRADO.plusYears(1), leido.getGraduationDate());
		assertEquals("Andes", leido.getUniversityName());
		assertEquals(1, mongo.getCollection("estudios").countDocuments(new Document("primaryPersona", CC_UNO)));
	}

	@Test
	void listarYPaginarOrdenanPorProfesionYLuegoPorPersona() {
		adaptador.save(estudio(PROF_DOS, CC_UNO, GRADO, "B"));
		adaptador.save(estudio(PROF_UNO, CC_DOS, GRADO, "A"));
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO, "A"));

		List<String> todos = adaptador.find().stream().map(StudyOutputAdapterMongoMotorTest::clave).toList();
		assertEquals(todos.stream().sorted().toList(), todos, "C-06, no el orden de inserción");
		assertTrue(todos.indexOf(clave(PROF_UNO, CC_UNO)) < todos.indexOf(clave(PROF_UNO, CC_DOS)));
		assertTrue(todos.indexOf(clave(PROF_UNO, CC_DOS)) < todos.indexOf(clave(PROF_DOS, CC_UNO)));

		int total = adaptador.count();
		List<String> recorridos = new ArrayList<>();
		for (int pagina = 0; pagina * 2 < total; pagina++)
			adaptador.find(pagina, 2).forEach(s -> recorridos.add(clave(s)));
		assertEquals(todos, recorridos, "C-07: las páginas recorren el mismo orden sin repetir");
		assertTrue(adaptador.find(10_000, 10).isEmpty());
	}

	@Test
	void contarYEliminarUsanElMotor() {
		int antes = adaptador.count();
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO, "A"));
		adaptador.save(estudio(PROF_DOS, CC_UNO, GRADO, "B"));
		assertEquals(antes + 2, adaptador.count());

		assertTrue(adaptador.delete(PROF_UNO, CC_UNO));
		assertNull(adaptador.findById(PROF_UNO, CC_UNO));
		assertNotNull(adaptador.findById(PROF_DOS, CC_UNO), "solo se borra el par indicado");
		assertEquals(antes + 1, adaptador.count());
	}

	@Test
	void elCasoDeUsoCompruebaLasReferenciasYLosDependientesEnElMotor() throws Exception {
		StudyUseCase estudios = new StudyUseCase(adaptador, personas, profesiones);
		assertThrows(ReferenceNotFoundException.class,
				() -> estudios.create(estudio(PROF_UNO, 990019, GRADO, "A")), "C-03: persona inexistente");
		assertThrows(ReferenceNotFoundException.class,
				() -> estudios.create(estudio(99019, CC_UNO, GRADO, "A")), "C-03: profesión inexistente");

		estudios.create(estudio(PROF_UNO, CC_UNO, GRADO, "A"));
		assertTrue(profesiones.hasStudies(PROF_UNO));
		assertFalse(profesiones.hasStudies(PROF_DOS));
		assertThrows(DependentRecordsException.class, () -> new ProfessionUseCase(profesiones).drop(PROF_UNO),
				"C-05: la profesión con estudios no se elimina");
	}

	private static String clave(int prof, int cc) {
		return String.format("%06d-%07d", prof, cc);
	}

	private static String clave(Study s) {
		return clave(s.getProfession().getIdentification(), s.getPerson().getIdentification());
	}
}
