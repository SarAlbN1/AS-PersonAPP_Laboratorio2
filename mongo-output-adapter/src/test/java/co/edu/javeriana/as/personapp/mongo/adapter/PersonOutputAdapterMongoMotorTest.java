package co.edu.javeriana.as.personapp.mongo.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.mongo.repository.PersonaRepositoryMongo;

/**
 * HU-013: ejercita las seis operaciones del adaptador contra un MongoDB real, no
 * contra un motor simulado. Requiere los contenedores de HU-034 arriba y corre
 * solo con -Dmotores=arriba:
 *
 * <pre>
 * docker compose up -d mariadb mongo
 * ./mvnw -B -ntp -pl mongo-output-adapter -am -Dmotores=arriba test
 * </pre>
 *
 * Usa cédulas 99000x y las borra al terminar cada caso.
 */
@DataMongoTest
@TestPropertySource(properties = {
		"spring.mongodb.uri=mongodb://persona_db:persona_db@localhost:27017/persona_db?authSource=admin",
		// los mappers de la plantilla se referencian en ciclo, igual que en la aplicación
		"spring.main.allow-circular-references=true" })
@EnabledIfSystemProperty(named = "motores", matches = "arriba")
class PersonOutputAdapterMongoMotorTest {

	private static final int CC_UNO = 990001;
	private static final int CC_DOS = 990002;
	private static final int CC_TRES = 990003;

	@Autowired
	private PersonOutputAdapterMongo adaptador;

	@Autowired
	private PersonaRepositoryMongo repositorio;

	@AfterEach
	void limpiarRegistrosDePrueba() {
		for (int cc : new int[] { CC_UNO, CC_DOS, CC_TRES }) {
			repositorio.findById(cc).ifPresent(repositorio::delete);
		}
	}

	private Person persona(int cc, String nombre) {
		Person person = new Person();
		person.setIdentification(cc);
		person.setFirstName(nombre);
		person.setLastName("HU013");
		person.setGender(Gender.FEMALE);
		person.setAge(41);
		return person;
	}

	@Test
	void guardaYConsultaPorIdentificadorConservandoLosCampos() {
		adaptador.save(persona(CC_UNO, "Integra"));
		Person leida = adaptador.findById(CC_UNO);
		assertNotNull(leida);
		assertEquals(CC_UNO, leida.getIdentification());
		assertEquals("Integra", leida.getFirstName());
		assertEquals("HU013", leida.getLastName());
		assertEquals(Gender.FEMALE, leida.getGender());
		assertEquals(41, leida.getAge());
	}

	@Test
	void consultarUnIdentificadorAusenteDevuelveNulo() {
		assertNull(adaptador.findById(CC_TRES));
	}

	@Test
	void contarUsaElMotorYCreceConCadaAlta() {
		Integer antes = adaptador.count();
		adaptador.save(persona(CC_UNO, "Una"));
		adaptador.save(persona(CC_DOS, "Dos"));
		assertEquals(antes + 2, adaptador.count());
	}

	@Test
	void listarTodosDevuelveLosRegistrosOrdenadosPorClave() {
		adaptador.save(persona(CC_DOS, "Dos"));
		adaptador.save(persona(CC_UNO, "Una"));
		List<Integer> claves = adaptador.find().stream().map(Person::getIdentification).toList();
		assertTrue(claves.indexOf(CC_UNO) < claves.indexOf(CC_DOS), "C-06 exige orden ascendente por clave");
		assertEquals(claves.stream().sorted().toList(), claves);
	}

	@Test
	void listarParcialPaginaConElMismoOrdenYSinRepetir() {
		adaptador.save(persona(CC_UNO, "Una"));
		adaptador.save(persona(CC_DOS, "Dos"));
		adaptador.save(persona(CC_TRES, "Tres"));
		int total = adaptador.count();
		List<Integer> recorridas = new ArrayList<>();
		for (int pagina = 0; pagina * 2 < total; pagina++) {
			adaptador.find(pagina, 2).forEach(persona -> recorridas.add(persona.getIdentification()));
		}
		assertEquals(total, recorridas.size());
		assertEquals(recorridas.stream().distinct().toList(), recorridas);
		assertEquals(recorridas.stream().sorted().toList(), recorridas);
	}

	@Test
	void unaPaginaFueraDeRangoDevuelveListaVacia() {
		assertTrue(adaptador.find(10_000, 10).isEmpty());
	}

	@Test
	void editaYElimina() {
		adaptador.save(persona(CC_UNO, "Original"));
		adaptador.save(persona(CC_UNO, "Editada"));
		assertEquals("Editada", adaptador.findById(CC_UNO).getFirstName());
		assertTrue(adaptador.delete(CC_UNO));
		assertNull(adaptador.findById(CC_UNO));
	}
}
