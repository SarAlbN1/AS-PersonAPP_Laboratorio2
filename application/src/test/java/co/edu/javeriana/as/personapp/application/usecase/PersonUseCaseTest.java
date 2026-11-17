package co.edu.javeriana.as.personapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;

/**
 * Verifica las seis operaciones de Persona en la capa de puertos y casos de uso
 * (HU-012), con un puerto de salida de prueba. No usa MariaDB ni MongoDB.
 */
class PersonUseCaseTest {

	private PuertoDePrueba puerto;
	private PersonUseCase caso;

	@BeforeEach
	void prepararCasoDeUso() {
		puerto = new PuertoDePrueba();
		caso = new PersonUseCase(puerto);
	}

	private Person persona(Integer cc, String nombre) {
		Person person = new Person();
		person.setIdentification(cc);
		person.setFirstName(nombre);
		person.setLastName("Prueba");
		person.setGender(Gender.FEMALE);
		person.setAge(30);
		return person;
	}

	@Test
	void crearDelegaEnElPuertoDeSalida() throws Exception {
		Person nueva = persona(1, "Ana");
		assertSame(nueva, caso.create(nueva));
		// C-02: antes de guardar consulta la clave para rechazar el duplicado.
		assertEquals(List.of("findById", "save"), puerto.llamadas);
		assertEquals(1, puerto.almacen.size());
	}

	@Test
	void consultarPorIdentificadorDevuelveElRegistro() throws Exception {
		puerto.guardar(persona(1, "Ana"));
		assertEquals("Ana", caso.findOne(1).getFirstName());
	}

	@Test
	void consultarUnIdentificadorAusenteInformaQueNoExiste() {
		assertThrows(NoExistException.class, () -> caso.findOne(99));
	}

	@Test
	void listarTodosDelegaEnElPuertoDeSalida() throws Exception {
		puerto.guardar(persona(2, "Beatriz"));
		puerto.guardar(persona(1, "Ana"));
		assertEquals(2, caso.findAll().size());
		assertTrue(puerto.llamadas.contains("find"));
	}

	@Test
	void contarUsaElConteoDelMotorYNoElTamanioDeLaLista() throws Exception {
		puerto.guardar(persona(1, "Ana"));
		puerto.guardar(persona(2, "Beatriz"));
		assertEquals(2, caso.count());
		// La unica llamada es count: el caso de uso ya no recorre findAll para contar.
		assertEquals(List.of("count"), puerto.llamadas);
	}

	@Test
	void contarUnaColeccionVaciaDevuelveCero() {
		assertEquals(0, caso.count());
	}

	@Test
	void listarParcialPideLaPaginaAlPuertoDeSalida() throws InvalidDataException {
		puerto.guardar(persona(1, "Ana"));
		puerto.guardar(persona(2, "Beatriz"));
		puerto.guardar(persona(3, "Carmen"));
		List<Person> pagina = caso.findAll(1, 2);
		assertEquals(1, pagina.size());
		assertEquals("Carmen", pagina.get(0).getFirstName());
		assertEquals("find:1:2", puerto.llamadas.get(puerto.llamadas.size() - 1));
	}

	@Test
	void unaPaginaFueraDeRangoDevuelveUnaListaVacia() throws InvalidDataException {
		puerto.guardar(persona(1, "Ana"));
		assertTrue(caso.findAll(5, 10).isEmpty());
	}

	@Test
	void losParametrosInvalidosDeLaPaginaSeRechazan() {
		assertThrows(InvalidDataException.class, () -> caso.findAll(-1, 10));
		assertThrows(InvalidDataException.class, () -> caso.findAll(0, 0));
		assertThrows(InvalidDataException.class, () -> caso.findAll(0, 101));
		assertThrows(InvalidDataException.class, () -> caso.findAll(null, 10));
		assertThrows(InvalidDataException.class, () -> caso.findAll(0, null));
		assertTrue(puerto.llamadas.isEmpty());
	}

	@Test
	void editarUnRegistroExistenteLoGuarda() throws Exception {
		puerto.guardar(persona(1, "Ana"));
		Person editada = persona(1, "Ana Maria");
		assertEquals("Ana Maria", caso.edit(1, editada).getFirstName());
	}

	@Test
	void rechazaElAltaDuplicadaSinSobrescribir() throws Exception {
		caso.create(persona(1, "Ana"));
		Person repetida = persona(1, "Otra");
		assertThrows(DuplicateException.class, () -> caso.create(repetida));
		assertEquals("Ana", caso.findOne(1).getFirstName());
	}

	@Test
	void rechazaLosDatosInvalidosDelContrato() {
		assertThrows(InvalidDataException.class, () -> caso.create(null));
		assertThrows(InvalidDataException.class, () -> caso.create(persona(0, "Ana")));
		assertThrows(InvalidDataException.class, () -> caso.create(persona(1, "   ")));
		assertThrows(InvalidDataException.class, () -> caso.create(persona(1, "A".repeat(46))));
		Person sinGenero = persona(1, "Ana");
		sinGenero.setGender(Gender.OTHER);
		assertThrows(InvalidDataException.class, () -> caso.create(sinGenero));
		Person edadNegativa = persona(1, "Ana");
		edadNegativa.setAge(-1);
		assertThrows(InvalidDataException.class, () -> caso.create(edadNegativa));
		assertTrue(puerto.almacen.isEmpty());
	}

	@Test
	void recortaLosNombresYAceptaElLimiteDelEsquema() throws Exception {
		Person conEspacios = persona(1, "  Ana  ");
		assertEquals("Ana", caso.create(conEspacios).getFirstName());
		Person alLimite = persona(2, "A".repeat(45));
		assertEquals(45, caso.create(alLimite).getFirstName().length());
	}

	@Test
	void aceptaLaEdadAusente() throws Exception {
		Person sinEdad = persona(1, "Ana");
		sinEdad.setAge(null);
		assertNull(caso.create(sinEdad).getAge());
	}

	@Test
	void editarConOtraClaveEnElCuerpoSeRechaza() throws Exception {
		caso.create(persona(1, "Ana"));
		Person otraClave = persona(2, "Ana");
		assertThrows(KeyMismatchException.class, () -> caso.edit(1, otraClave));
		assertEquals("Ana", caso.findOne(1).getFirstName());
	}

	@Test
	void editarUnIdentificadorAusenteInformaQueNoExiste() {
		assertThrows(NoExistException.class, () -> caso.edit(99, persona(99, "Nadie")));
	}

	@Test
	void eliminarUnRegistroExistenteLoRetira() throws Exception {
		puerto.guardar(persona(1, "Ana"));
		assertTrue(caso.drop(1));
		assertEquals(0, caso.count());
	}

	@Test
	void eliminarEsRestrictivoConTelefonosOEstudios() throws Exception {
		puerto.guardar(persona(1, "Con telefonos"));
		puerto.guardar(persona(2, "Con estudios"));
		puerto.conTelefonos.add(1);
		puerto.conEstudios.add(2);
		assertThrows(DependentRecordsException.class, () -> caso.drop(1));
		assertThrows(DependentRecordsException.class, () -> caso.drop(2));
		// C-05: se revisó antes de llamar a delete, así que ningún registro se perdió.
		assertEquals(2, caso.count());
		assertTrue(puerto.llamadas.stream().noneMatch("delete"::equals));
	}

	@Test
	void eliminarUnIdentificadorAusenteInformaQueNoExiste() {
		assertThrows(NoExistException.class, () -> caso.drop(99));
	}

	@Test
	void cambiarDePersistenciaUsaElNuevoPuerto() throws Exception {
		PuertoDePrueba otro = new PuertoDePrueba();
		otro.guardar(persona(7, "Otra"));
		caso.setPersintence(otro);
		assertEquals(1, caso.count());
	}

	/** Puerto de salida en memoria: ordena por clave y pagina como exige el contrato. */
	private static class PuertoDePrueba implements PersonOutputPort {

		private final Map<Integer, Person> almacen = new LinkedHashMap<>();
		private final List<String> llamadas = new ArrayList<>();

		void guardar(Person person) {
			almacen.put(person.getIdentification(), person);
		}

		@Override
		public Person save(Person person) {
			llamadas.add("save");
			guardar(person);
			return person;
		}

		@Override
		public Boolean delete(Integer identification) {
			llamadas.add("delete");
			return almacen.remove(identification) != null;
		}

		@Override
		public List<Person> find() {
			llamadas.add("find");
			return ordenadas();
		}

		@Override
		public Person findById(Integer identification) {
			llamadas.add("findById");
			return almacen.get(identification);
		}

		private final java.util.Set<Integer> conTelefonos = new java.util.HashSet<>();
		private final java.util.Set<Integer> conEstudios = new java.util.HashSet<>();

		@Override
		public Boolean hasPhones(Integer identification) {
			llamadas.add("hasPhones");
			return conTelefonos.contains(identification);
		}

		@Override
		public Boolean hasStudies(Integer identification) {
			llamadas.add("hasStudies");
			return conEstudios.contains(identification);
		}

		@Override
		public Integer count() {
			llamadas.add("count");
			return almacen.size();
		}

		@Override
		public List<Person> find(Integer page, Integer size) {
			llamadas.add("find:" + page + ":" + size);
			List<Person> todas = ordenadas();
			int desde = page * size;
			if (desde >= todas.size())
				return List.of();
			return todas.subList(desde, Math.min(desde + size, todas.size()));
		}

		private List<Person> ordenadas() {
			Person[] copia = almacen.values().toArray(new Person[0]);
			Arrays.sort(copia, (una, otra) -> una.getIdentification() - otra.getIdentification());
			return new ArrayList<>(Arrays.asList(copia));
		}
	}
}
