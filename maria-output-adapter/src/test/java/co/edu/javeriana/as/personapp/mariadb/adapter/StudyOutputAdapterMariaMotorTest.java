package co.edu.javeriana.as.personapp.mariadb.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
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
 * HU-023: ejercita la persistencia de Estudios contra un MariaDB real, con la FK y la
 * clave compuesta del DDL. Corre solo con -Dmotores=arriba y las bases publicadas:
 *
 * <pre>
 * docker compose -f compose.yaml -f compose.pruebas.yaml up -d mariadb mongo
 * ./mvnw -B -ntp -pl maria-output-adapter -am -Dmotores=arriba test
 * </pre>
 *
 * Usa cédulas 99001x y profesiones 9901x dentro de una transacción que se revierte.
 * Cada lectura vacía antes el contexto de persistencia, así que va al motor.
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
class StudyOutputAdapterMariaMotorTest {

	private static final int CC_UNO = 990011;
	private static final int CC_DOS = 990012;
	private static final int PROF_UNO = 99011;
	private static final int PROF_DOS = 99012;
	private static final LocalDate GRADO = LocalDate.of(2015, 12, 1);

	@Autowired
	private StudyOutputAdapterMaria adaptador;

	@Autowired
	private PersonOutputAdapterMaria personas;

	@Autowired
	private ProfessionOutputAdapterMaria profesiones;

	@Autowired
	private TestEntityManager em;

	@BeforeEach
	void crearReferencias() {
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
		sincronizar();
	}

	private void sincronizar() {
		em.flush();
		em.clear();
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
		sincronizar();

		Study leido = adaptador.findById(PROF_UNO, CC_UNO);
		assertNotNull(leido);
		assertEquals(CC_UNO, leido.getPerson().getIdentification());
		assertEquals(PROF_UNO, leido.getProfession().getIdentification());
		assertEquals("Prof " + PROF_UNO, leido.getProfession().getName());
		assertEquals(GRADO, leido.getGraduationDate());
		assertEquals("Javeriana", leido.getUniversityName());
		assertNull(leido.getPerson().getStudies(), "C-10: no se cargan las colecciones de la persona");
		assertNull(adaptador.findById(PROF_DOS, CC_UNO), "la otra mitad de la clave no basta");
	}

	@Test
	void fechaYUniversidadSonOpcionales() {
		adaptador.save(estudio(PROF_UNO, CC_UNO, null, null));
		sincronizar();
		Study leido = adaptador.findById(PROF_UNO, CC_UNO);
		assertNull(leido.getGraduationDate());
		assertNull(leido.getUniversityName(), "univer es NULL en la base, no texto vacío");
	}

	@Test
	void editarCambiaFechaYUniversidadSinCambiarLaClave() {
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO, "Javeriana"));
		sincronizar();
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO.plusYears(1), "Andes"));
		sincronizar();
		Study leido = adaptador.findById(PROF_UNO, CC_UNO);
		assertEquals(GRADO.plusYears(1), leido.getGraduationDate());
		assertEquals("Andes", leido.getUniversityName());
	}

	@Test
	void listarYPaginarOrdenanPorProfesionYLuegoPorPersona() {
		adaptador.save(estudio(PROF_DOS, CC_UNO, GRADO, "B"));
		adaptador.save(estudio(PROF_UNO, CC_DOS, GRADO, "A"));
		adaptador.save(estudio(PROF_UNO, CC_UNO, GRADO, "A"));
		sincronizar();

		List<String> todos = adaptador.find().stream().map(StudyOutputAdapterMariaMotorTest::clave).toList();
		assertEquals(todos.stream().sorted().toList(), todos, "C-06");
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
		sincronizar();
		assertEquals(antes + 2, adaptador.count());

		assertTrue(adaptador.delete(PROF_UNO, CC_UNO));
		sincronizar();
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
		sincronizar();
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
