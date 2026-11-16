package co.edu.javeriana.as.personapp.terminal.menu;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.EstudioInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.EstudioModelCli;

/** HU-024: el menú de Estudios pide la clave en dos partes y los errores no cierran la sesión. */
class EstudioMenuTest {

	private final PrintStream original = System.out;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private EstudioInputAdapterCli adapter;

	@BeforeEach
	void setUp() {
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
		adapter = mock(EstudioInputAdapterCli.class);
	}

	@AfterEach
	void tearDown() {
		System.setOut(original);
	}

	private String run(String input) {
		new EstudioMenu().iniciarMenu(adapter, new Scanner(input));
		return out.toString(StandardCharsets.UTF_8);
	}

	@Test
	void alternaMotoresEnLaMismaSesion() throws Exception {
		when(adapter.contar(DatabaseOption.MARIA)).thenReturn(2);
		when(adapter.contar(DatabaseOption.MONGO)).thenReturn(3);
		String salida = run("1\n3\n0\n2\n3\n0\n0\n");
		assertTrue(salida.contains("Total en MARIA: 2"), salida);
		assertTrue(salida.contains("Total en MONGO: 3"), salida);
	}

	@Test
	void consultaConLaClaveEnDosPartesProfesionYLuegoPersona() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, 1, 123456789))
				.thenReturn(new EstudioModelCli(1, 123456789, LocalDate.of(2015, 12, 1), "Javeriana"));
		String salida = run("1\n1\n1\n123456789\n0\n0\n");
		assertTrue(salida.contains("Id de la profesión: Cédula de la persona: "), salida);
		assertTrue(salida.contains("Estudio [profesión=1, persona=123456789, fecha=2015-12-01, universidad=Javeriana]"),
				salida);
	}

	@Test
	void creaConFechaYUniversidadDeVariasPalabras() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MONGO), any())).thenAnswer(i -> i.getArgument(1));
		String salida = run("2\n4\n2\n321654987\n2018-06-30\nUniversidad Nacional\n0\n0\n");
		verify(adapter).crear(DatabaseOption.MONGO,
				new EstudioModelCli(2, 321654987, LocalDate.of(2018, 6, 30), "Universidad Nacional"));
		assertTrue(salida.contains("Creado: Estudio [profesión=2, persona=321654987, fecha=2018-06-30"), salida);
	}

	@Test
	void fechaYUniversidadSonOpcionalesYUnaFechaInvalidaSeVuelveAPedir() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MARIA), any())).thenAnswer(i -> i.getArgument(1));
		String salida = run("1\n4\n1\n123456789\n2015-13-45\n01/12/2015\n\n\n0\n0\n");
		assertTrue(salida.contains("[ERR-01] La fecha '2015-13-45' no es válida; use el formato yyyy-MM-dd"), salida);
		assertTrue(salida.contains("[ERR-01] La fecha '01/12/2015' no es válida"), salida);
		verify(adapter).crear(DatabaseOption.MARIA, new EstudioModelCli(1, 123456789, null, null));
	}

	@Test
	void editarMuestraElActualYConservaLaClave() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, 1, 123456789))
				.thenReturn(new EstudioModelCli(1, 123456789, LocalDate.of(2015, 12, 1), "Javeriana"));
		when(adapter.editar(eq(DatabaseOption.MARIA), eq(1), eq(123456789), any())).thenAnswer(i -> i.getArgument(3));
		String salida = run("1\n5\n1\n123456789\n2016-01-15\nAndes\n0\n0\n");
		verify(adapter).editar(DatabaseOption.MARIA, 1, 123456789,
				new EstudioModelCli(1, 123456789, LocalDate.of(2016, 1, 15), "Andes"));
		assertTrue(salida.contains("Actual: Estudio [profesión=1, persona=123456789, fecha=2015-12-01"), salida);
		assertTrue(salida.contains("Editado: Estudio [profesión=1, persona=123456789, fecha=2016-01-15, universidad=Andes]"),
				salida);
	}

	@Test
	void erroresMuestranCodigoYMotorYSigueLaSesion() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MONGO), any()))
				.thenThrow(new ReferenceNotFoundException("No existe una profesión con id 99"))
				.thenThrow(new DuplicateException("Ya existe un estudio de la profesión 1 para la persona 123456789"));
		doThrow(new NoExistException("No existe un estudio de la profesión 9 para la persona 9")).when(adapter)
				.eliminar(DatabaseOption.MONGO, 9, 9);
		when(adapter.listarPagina(DatabaseOption.MONGO, 0, 2))
				.thenReturn(List.of(new EstudioModelCli(1, 123456789, null, null)));
		String salida = run("2\n4\n99\n123456789\n\n\n4\n1\n123456789\n\n\n6\n9\n9\n7\n0\n2\n0\n0\n");
		assertTrue(salida.contains("[ERR-05] No existe una profesión con id 99 en MONGO"), salida);
		assertTrue(salida.contains("[ERR-04] Ya existe un estudio de la profesión 1 para la persona 123456789 en MONGO"),
				salida);
		assertTrue(salida.contains("[ERR-03] No existe un estudio de la profesión 9 para la persona 9 en MONGO"), salida);
		assertTrue(salida.contains("Página 0 · 1 registros"), salida);
	}

	@Test
	void claveNoNumericaSeVuelveAPedir() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, 1, 123456789))
				.thenReturn(new EstudioModelCli(1, 123456789, null, null));
		String salida = run("1\n1\nuno\n1\nabc\n123456789\n0\n0\n");
		assertTrue(salida.contains("[ERR-01] 'uno' no es un número entero"), salida);
		assertTrue(salida.contains("[ERR-01] 'abc' no es un número entero"), salida);
		verify(adapter).consultar(DatabaseOption.MARIA, 1, 123456789);
	}

	@Test
	void motorInvalidoEsErr02() {
		assertTrue(run("5\n0\n").contains("[ERR-02]"));
	}
}
