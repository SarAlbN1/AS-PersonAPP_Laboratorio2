package co.edu.javeriana.as.personapp.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mapper.EstudioMapperRest;
import co.edu.javeriana.as.personapp.model.response.EstudioResponse;

/**
 * HU-025 para Estudios: el motor se elige por petición y el adaptador no guarda estado
 * compartido. Es el mismo caso que {@link SeleccionDinamicaRestTest} y
 * {@link SeleccionDinamicaProfesionTelefonoRestTest}. Estudios tiene además clave compuesta
 * y comprueba persona y profesión en el mismo motor, así que aquí cada motor es un conjunto
 * de tres puertos que devuelven la universidad marcada con su nombre.
 */
class SeleccionDinamicaEstudioRestTest {

	private static final int HILOS = 16;
	private static final int SOLICITUDES_POR_HILO = 200;

	private static void poner(Object destino, String campo, Object valor) {
		try {
			Field f = destino.getClass().getDeclaredField(campo);
			f.setAccessible(true);
			f.set(destino, valor);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Object leer(Object origen, String campo) {
		try {
			Field f = origen.getClass().getDeclaredField(campo);
			f.setAccessible(true);
			return f.get(origen);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private EstudioInputAdapterRest adaptador() {
		EstudioInputAdapterRest adaptador = new EstudioInputAdapterRest();
		poner(adaptador, "studyOutputPortMaria", new EstudioUnMotor("MARIA"));
		poner(adaptador, "studyOutputPortMongo", new EstudioUnMotor("MONGO"));
		poner(adaptador, "personOutputPortMaria", new PersonaDeEstudio());
		poner(adaptador, "personOutputPortMongo", new PersonaDeEstudio());
		poner(adaptador, "professionOutputPortMaria", new ProfesionDeEstudio());
		poner(adaptador, "professionOutputPortMongo", new ProfesionDeEstudio());
		poner(adaptador, "estudioMapperRest", new EstudioMapperRest());
		return adaptador;
	}

	@Test
	void alternaDeMotorEnUnMismoProceso() throws Exception {
		EstudioInputAdapterRest adaptador = adaptador();
		for (int i = 0; i < 6; i++) {
			String motor = i % 2 == 0 ? "MARIA" : "MONGO";
			assertEquals("de-" + motor, adaptador.consultar(motor, 1, 1).universityName());
		}
		assertEquals("de-MONGO", adaptador.consultar("mongo", 1, 1).universityName());
	}

	@Test
	void conMotorInvalidoEsErr02SinMotorPorDefecto() throws Exception {
		EstudioInputAdapterRest adaptador = adaptador();
		assertEquals("de-MARIA", adaptador.consultar("MARIA", 1, 1).universityName());
		assertThrows(InvalidOptionException.class, () -> adaptador.consultar("ORACLE", 1, 1));
		assertThrows(InvalidOptionException.class, () -> adaptador.consultar(null, 1, 1));
	}

	@Test
	void concurrenteNoCruzaDatosEntreMotores() throws Exception {
		EstudioInputAdapterRest adaptador = adaptador();
		ExecutorService pool = Executors.newFixedThreadPool(HILOS);
		CountDownLatch salida = new CountDownLatch(1);
		AtomicInteger cruzadas = new AtomicInteger();
		List<Future<Void>> tareas = new ArrayList<>();
		for (int h = 0; h < HILOS; h++) {
			final String motor = h % 2 == 0 ? "MARIA" : "MONGO";
			Callable<Void> tarea = () -> {
				salida.await();
				for (int i = 0; i < SOLICITUDES_POR_HILO; i++) {
					EstudioResponse r = adaptador.consultar(motor, 1, 1);
					if (!("de-" + motor).equals(r.universityName()))
						cruzadas.incrementAndGet();
				}
				return null;
			};
			tareas.add(pool.submit(tarea));
		}
		salida.countDown();
		for (Future<Void> tarea : tareas)
			tarea.get(30, TimeUnit.SECONDS);
		pool.shutdownNow();
		assertEquals(0, cruzadas.get(), "ningún estudio debe traer datos del otro motor");
	}

	@Test
	void escribirEnUnMotorNoTocaAlOtro() throws Exception {
		EstudioInputAdapterRest adaptador = adaptador();
		adaptador.eliminar("MONGO", 1, 1);
		EstudioUnMotor maria = (EstudioUnMotor) leer(adaptador, "studyOutputPortMaria");
		EstudioUnMotor mongo = (EstudioUnMotor) leer(adaptador, "studyOutputPortMongo");
		assertEquals(List.of("delete"), mongo.escrituras);
		assertTrue(maria.escrituras.isEmpty(), "borrar en MONGO no debe tocar MARIA");
	}

	// ------------------------------------------------------------------ puertos

	private static Person persona() {
		Person person = new Person();
		person.setIdentification(1);
		person.setFirstName("Persona");
		person.setLastName("Prueba");
		person.setGender(Gender.FEMALE);
		return person;
	}

	private static Profession profesion() {
		Profession profession = new Profession();
		profession.setIdentification(1);
		profession.setName("Profesion");
		return profession;
	}

	/** Estudios de un solo motor: devuelve el estudio (1, 1) con la universidad marcada con el motor. */
	private static class EstudioUnMotor implements StudyOutputPort {
		private final String motor;
		private final List<String> escrituras = new CopyOnWriteArrayList<>();

		EstudioUnMotor(String motor) {
			this.motor = motor;
		}

		private Study estudio() {
			Study study = new Study();
			study.setPerson(persona());
			study.setProfession(profesion());
			study.setGraduationDate(LocalDate.of(2020, 5, 15));
			study.setUniversityName("de-" + motor);
			return study;
		}

		public Study save(Study study) {
			escrituras.add("save");
			return study;
		}

		public Boolean delete(Integer professionId, Integer personId) {
			escrituras.add("delete");
			return true;
		}

		public List<Study> find() {
			return List.of(estudio());
		}

		public List<Study> find(Integer page, Integer size) {
			return find();
		}

		public Study findById(Integer professionId, Integer personId) {
			return estudio();
		}

		public Integer count() {
			return 1;
		}
	}

	/** La persona existe en los dos motores; no interviene en lo que se comprueba aquí. */
	private static class PersonaDeEstudio implements PersonOutputPort {
		public Person save(Person person) {
			return person;
		}

		public Boolean delete(Integer identification) {
			return true;
		}

		public List<Person> find() {
			return List.of(persona());
		}

		public Person findById(Integer identification) {
			return persona();
		}

		public Integer count() {
			return 1;
		}

		public List<Person> find(Integer page, Integer size) {
			return find();
		}

		public Boolean hasPhones(Integer identification) {
			return false;
		}

		public Boolean hasStudies(Integer identification) {
			return false;
		}
	}

	/** La profesión existe en los dos motores; no interviene en lo que se comprueba aquí. */
	private static class ProfesionDeEstudio implements ProfessionOutputPort {
		public Profession save(Profession profession) {
			return profession;
		}

		public Boolean delete(Integer identification) {
			return true;
		}

		public List<Profession> find() {
			return List.of(profesion());
		}

		public List<Profession> find(Integer page, Integer size) {
			return find();
		}

		public Profession findById(Integer identification) {
			return profesion();
		}

		public Integer count() {
			return 1;
		}

		public Boolean hasStudies(Integer identification) {
			return false;
		}
	}
}
