package co.edu.javeriana.as.personapp.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
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
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mapper.ProfesionMapperRest;
import co.edu.javeriana.as.personapp.mapper.TelefonoMapperRest;
import co.edu.javeriana.as.personapp.model.response.ProfesionResponse;
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;

/**
 * HU-025 para Profesión y Teléfono: el motor se elige por petición y los adaptadores no
 * guardan estado compartido. Es el mismo caso que {@link SeleccionDinamicaRestTest} aplicado a
 * los otros dos recursos que ya están expuestos. Cada motor es un puerto distinto que
 * devuelve datos marcados con su nombre; si el adaptador guardara el motor en un campo,
 * solicitudes concurrentes cruzarían respuestas de un motor al otro.
 */
class SeleccionDinamicaProfesionTelefonoRestTest {

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

	/** Lanza solicitudes alternadas desde varios hilos y cuenta las que traen datos del otro motor. */
	private static int cruzadas(java.util.function.BiPredicate<String, Integer> solicitudCorrecta) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(HILOS);
		CountDownLatch salida = new CountDownLatch(1);
		AtomicInteger cruzadas = new AtomicInteger();
		List<Future<Void>> tareas = new ArrayList<>();
		for (int h = 0; h < HILOS; h++) {
			final String motor = h % 2 == 0 ? "MARIA" : "MONGO";
			Callable<Void> tarea = () -> {
				salida.await();
				for (int i = 0; i < SOLICITUDES_POR_HILO; i++)
					if (!solicitudCorrecta.test(motor, i))
						cruzadas.incrementAndGet();
				return null;
			};
			tareas.add(pool.submit(tarea));
		}
		salida.countDown();
		for (Future<Void> tarea : tareas)
			tarea.get(30, TimeUnit.SECONDS);
		pool.shutdownNow();
		return cruzadas.get();
	}

	// ---------------------------------------------------------------- Profesión

	private ProfesionInputAdapterRest profesion() {
		ProfesionInputAdapterRest adaptador = new ProfesionInputAdapterRest();
		poner(adaptador, "professionOutputPortMaria", new ProfesionUnMotor("MARIA"));
		poner(adaptador, "professionOutputPortMongo", new ProfesionUnMotor("MONGO"));
		poner(adaptador, "profesionMapperRest", new ProfesionMapperRest());
		return adaptador;
	}

	@Test
	void profesionAlternaDeMotorEnUnMismoProceso() throws Exception {
		ProfesionInputAdapterRest adaptador = profesion();
		for (int i = 0; i < 6; i++) {
			String motor = i % 2 == 0 ? "MARIA" : "MONGO";
			assertEquals("de-" + motor, adaptador.consultar(motor, 1).name());
		}
		assertEquals("de-MONGO", adaptador.consultar("mongo", 1).name());
	}

	@Test
	void profesionConMotorInvalidoEsErr02SinMotorPorDefecto() throws Exception {
		ProfesionInputAdapterRest adaptador = profesion();
		assertEquals("de-MARIA", adaptador.consultar("MARIA", 1).name());
		assertThrows(InvalidOptionException.class, () -> adaptador.consultar("ORACLE", 1));
		assertThrows(InvalidOptionException.class, () -> adaptador.consultar(null, 1));
	}

	@Test
	void profesionConcurrenteNoCruzaDatosEntreMotores() throws Exception {
		ProfesionInputAdapterRest adaptador = profesion();
		int cruzadas = cruzadas((motor, i) -> {
			try {
				ProfesionResponse r = adaptador.consultar(motor, 1);
				return ("de-" + motor).equals(r.name());
			} catch (Exception e) {
				return false;
			}
		});
		assertEquals(0, cruzadas, "ninguna profesión debe traer datos del otro motor");
	}

	@Test
	void profesionEscribirEnUnMotorNoTocaAlOtro() throws Exception {
		ProfesionInputAdapterRest adaptador = profesion();
		adaptador.eliminar("MARIA", 1);
		ProfesionUnMotor maria = (ProfesionUnMotor) leer(adaptador, "professionOutputPortMaria");
		ProfesionUnMotor mongo = (ProfesionUnMotor) leer(adaptador, "professionOutputPortMongo");
		assertEquals(List.of("delete"), maria.escrituras);
		assertTrue(mongo.escrituras.isEmpty(), "borrar en MARIA no debe tocar MONGO");
	}

	// ----------------------------------------------------------------- Teléfono

	private TelefonoInputAdapterRest telefono() {
		TelefonoInputAdapterRest adaptador = new TelefonoInputAdapterRest();
		poner(adaptador, "phoneOutputPortMaria", new TelefonoUnMotor("MARIA"));
		poner(adaptador, "phoneOutputPortMongo", new TelefonoUnMotor("MONGO"));
		poner(adaptador, "personOutputPortMaria", new PersonaUnMotor());
		poner(adaptador, "personOutputPortMongo", new PersonaUnMotor());
		poner(adaptador, "telefonoMapperRest", new TelefonoMapperRest());
		return adaptador;
	}

	@Test
	void telefonoAlternaDeMotorEnUnMismoProceso() throws Exception {
		TelefonoInputAdapterRest adaptador = telefono();
		for (int i = 0; i < 6; i++) {
			String motor = i % 2 == 0 ? "MARIA" : "MONGO";
			assertEquals("de-" + motor, adaptador.consultar(motor, "3001234567").company());
		}
	}

	@Test
	void telefonoConMotorInvalidoEsErr02SinMotorPorDefecto() throws Exception {
		TelefonoInputAdapterRest adaptador = telefono();
		assertEquals("de-MONGO", adaptador.consultar("MONGO", "3001234567").company());
		assertThrows(InvalidOptionException.class, () -> adaptador.consultar("ORACLE", "3001234567"));
		assertThrows(InvalidOptionException.class, () -> adaptador.consultar(null, "3001234567"));
	}

	@Test
	void telefonoConcurrenteNoCruzaDatosEntreMotores() throws Exception {
		TelefonoInputAdapterRest adaptador = telefono();
		int cruzadas = cruzadas((motor, i) -> {
			try {
				TelefonoResponse r = adaptador.consultar(motor, "3001234567");
				return ("de-" + motor).equals(r.company());
			} catch (Exception e) {
				return false;
			}
		});
		assertEquals(0, cruzadas, "ningún teléfono debe traer datos del otro motor");
	}

	@Test
	void telefonoEscribirEnUnMotorNoTocaAlOtro() throws Exception {
		TelefonoInputAdapterRest adaptador = telefono();
		adaptador.eliminar("MONGO", "3001234567");
		TelefonoUnMotor maria = (TelefonoUnMotor) leer(adaptador, "phoneOutputPortMaria");
		TelefonoUnMotor mongo = (TelefonoUnMotor) leer(adaptador, "phoneOutputPortMongo");
		assertEquals(List.of("delete"), mongo.escrituras);
		assertTrue(maria.escrituras.isEmpty(), "borrar en MONGO no debe tocar MARIA");
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

	// ------------------------------------------------------------------ puertos

	private static Person dueno() {
		Person person = new Person();
		person.setIdentification(1);
		person.setFirstName("Dueno");
		person.setLastName("Prueba");
		person.setGender(Gender.FEMALE);
		return person;
	}

	/** Profesión de un solo motor: devuelve la profesión 1 marcada con el nombre del motor. */
	private static class ProfesionUnMotor implements ProfessionOutputPort {
		private final String motor;
		private final List<String> escrituras = new CopyOnWriteArrayList<>();

		ProfesionUnMotor(String motor) {
			this.motor = motor;
		}

		private Profession profesion() {
			Profession profession = new Profession();
			profession.setIdentification(1);
			profession.setName("de-" + motor);
			return profession;
		}

		public Profession save(Profession profession) {
			escrituras.add("save");
			return profession;
		}

		public Boolean delete(Integer identification) {
			escrituras.add("delete");
			return true;
		}

		public List<Profession> find() {
			return List.of(profesion());
		}

		public List<Profession> find(Integer page, Integer size) {
			return List.of(profesion());
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

	/** Teléfono de un solo motor: devuelve el teléfono marcado con el nombre del motor en el operador. */
	private static class TelefonoUnMotor implements PhoneOutputPort {
		private final String motor;
		private final List<String> escrituras = new CopyOnWriteArrayList<>();

		TelefonoUnMotor(String motor) {
			this.motor = motor;
		}

		private Phone telefono(String number) {
			Phone phone = new Phone();
			phone.setNumber(number);
			phone.setCompany("de-" + motor);
			phone.setOwner(dueno());
			return phone;
		}

		public Phone save(Phone phone) {
			escrituras.add("save");
			return phone;
		}

		public Boolean delete(String number) {
			escrituras.add("delete");
			return true;
		}

		public List<Phone> find() {
			return List.of(telefono("3001234567"));
		}

		public List<Phone> find(Integer page, Integer size) {
			return find();
		}

		public Phone findById(String number) {
			return telefono(number);
		}

		public Integer count() {
			return 1;
		}

		public List<Phone> findByOwner(Integer identification) {
			return find();
		}
	}

	/** La persona dueña existe en los dos motores; no interviene en lo que se comprueba aquí. */
	private static class PersonaUnMotor implements PersonOutputPort {
		public Person save(Person person) {
			return person;
		}

		public Boolean delete(Integer identification) {
			return true;
		}

		public List<Person> find() {
			return List.of(dueno());
		}

		public Person findById(Integer identification) {
			return dueno();
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
}
