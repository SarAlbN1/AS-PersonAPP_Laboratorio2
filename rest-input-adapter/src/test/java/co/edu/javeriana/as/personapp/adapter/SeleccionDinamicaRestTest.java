package co.edu.javeriana.as.personapp.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.mapper.PersonaMapperRest;
import co.edu.javeriana.as.personapp.model.response.PersonaResponse;

/**
 * HU-025: la selección del motor es por petición y no deja estado compartido.
 *
 * <p>Cada motor es un puerto de salida distinto que devuelve una persona marcada con su
 * nombre. Si el adaptador guardara el motor en un campo, como hacía la plantilla, dos
 * solicitudes concurrentes se pisarían y alguna respuesta traería los datos del otro
 * motor. Estas pruebas lanzan cientos de solicitudes alternadas desde varios hilos y
 * comprueban que ninguna cruza de motor.
 */
class SeleccionDinamicaRestTest {

	/** El adaptador inyecta por campo; en la prueba se asignan por reflexión, sin levantar Spring. */
	private static void poner(Object destino, String campo, Object valor) {
		try {
			java.lang.reflect.Field f = destino.getClass().getDeclaredField(campo);
			f.setAccessible(true);
			f.set(destino, valor);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Object leer(Object origen, String campo) {
		try {
			java.lang.reflect.Field f = origen.getClass().getDeclaredField(campo);
			f.setAccessible(true);
			return f.get(origen);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private PersonaInputAdapterRest adaptador() {
		PersonaInputAdapterRest adaptador = new PersonaInputAdapterRest();
		poner(adaptador, "personOutputPortMaria", new PuertoDeUnMotor("MARIA"));
		poner(adaptador, "personOutputPortMongo", new PuertoDeUnMotor("MONGO"));
		poner(adaptador, "personaMapperRest", new PersonaMapperRest());
		return adaptador;
	}

	@Test
	void solicitudesSucesivasDeUnMismoProcesoAlternanDeMotor() throws Exception {
		PersonaInputAdapterRest adaptador = adaptador();
		for (int i = 0; i < 6; i++) {
			String motor = i % 2 == 0 ? "MARIA" : "MONGO";
			PersonaResponse respuesta = adaptador.consultar(motor, 1);
			assertEquals(motor, respuesta.getDatabase(), "el rótulo debe ser el motor pedido");
			assertEquals("de-" + motor, respuesta.getFirstName(), "los datos deben salir del motor pedido");
		}
	}

	@Test
	void elMotorEsInsensibleAMayusculasYMinusculas() throws Exception {
		PersonaInputAdapterRest adaptador = adaptador();
		assertEquals("de-MONGO", adaptador.consultar("mongo", 1).getFirstName());
		assertEquals("de-MARIA", adaptador.consultar("Maria", 1).getFirstName());
	}

	@Test
	void unMotorInvalidoNoDejaElAnteriorSeleccionado() throws Exception {
		PersonaInputAdapterRest adaptador = adaptador();
		assertEquals("de-MARIA", adaptador.consultar("MARIA", 1).getFirstName());
		org.junit.jupiter.api.Assertions.assertThrows(co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException.class,
				() -> adaptador.consultar("ORACLE", 1));
		// ERR-02: no se usa un motor por defecto ni el de la solicitud anterior.
		org.junit.jupiter.api.Assertions.assertThrows(co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException.class,
				() -> adaptador.consultar(null, 1));
	}

	@Test
	void solicitudesConcurrentesNoCambianElMotorDeLasDemas() throws Exception {
		PersonaInputAdapterRest adaptador = adaptador();
		int hilos = 16;
		int solicitudesPorHilo = 200;
		ExecutorService pool = Executors.newFixedThreadPool(hilos);
		CountDownLatch salida = new CountDownLatch(1);
		AtomicInteger cruzadas = new AtomicInteger();
		AtomicInteger total = new AtomicInteger();
		List<Future<Void>> tareas = new ArrayList<>();
		for (int h = 0; h < hilos; h++) {
			final String motor = h % 2 == 0 ? "MARIA" : "MONGO";
			Callable<Void> tarea = () -> {
				salida.await();
				for (int i = 0; i < solicitudesPorHilo; i++) {
					PersonaResponse r = adaptador.consultar(motor, 1);
					total.incrementAndGet();
					if (!motor.equals(r.getDatabase()) || !("de-" + motor).equals(r.getFirstName()))
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
		assertEquals(hilos * solicitudesPorHilo, total.get());
		assertEquals(0, cruzadas.get(), "ninguna respuesta debe traer datos del otro motor");
	}

	@Test
	void unaEscrituraEnUnMotorNoTocaAlOtro() throws Exception {
		PersonaInputAdapterRest adaptador = adaptador();
		PuertoDeUnMotor maria = (PuertoDeUnMotor) leer(adaptador, "personOutputPortMaria");
		PuertoDeUnMotor mongo = (PuertoDeUnMotor) leer(adaptador, "personOutputPortMongo");
		adaptador.eliminar("MARIA", 1);
		assertEquals(List.of("delete"), maria.escrituras);
		assertTrue(mongo.escrituras.isEmpty(), "borrar en MARIA no debe tocar MONGO");
	}

	/** Puerto de salida de un solo motor: devuelve a la persona 1 marcada con el nombre del motor. */
	private static class PuertoDeUnMotor implements PersonOutputPort {

		private final String motor;
		private final List<String> escrituras = new java.util.concurrent.CopyOnWriteArrayList<>();

		PuertoDeUnMotor(String motor) {
			this.motor = motor;
		}

		private Person persona() {
			Person person = new Person();
			person.setIdentification(1);
			person.setFirstName("de-" + motor);
			person.setLastName("Prueba");
			person.setGender(Gender.FEMALE);
			person.setAge(30);
			return person;
		}

		@Override
		public Person findById(Integer identification) {
			return persona();
		}

		@Override
		public Boolean delete(Integer identification) {
			escrituras.add("delete");
			return true;
		}

		@Override
		public Person save(Person person) {
			escrituras.add("save");
			return person;
		}

		@Override
		public List<Person> find() {
			return List.of(persona());
		}

		@Override
		public List<Person> find(Integer page, Integer size) {
			return List.of(persona());
		}

		@Override
		public Integer count() {
			return 1;
		}

		@Override
		public Boolean hasPhones(Integer identification) {
			return false;
		}

		@Override
		public Boolean hasStudies(Integer identification) {
			return false;
		}
	}
}
