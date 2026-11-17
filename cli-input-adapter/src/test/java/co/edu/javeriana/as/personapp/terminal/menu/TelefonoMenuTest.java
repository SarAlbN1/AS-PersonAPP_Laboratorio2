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
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.TelefonoInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.TelefonoModelCli;

/** HU-021: el menú de Teléfono sigue el menú común y los errores no cierran la sesión. */
class TelefonoMenuTest {

	private final PrintStream original = System.out;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private TelefonoInputAdapterCli adapter;

	@BeforeEach
	void setUp() {
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
		adapter = mock(TelefonoInputAdapterCli.class);
	}

	@AfterEach
	void tearDown() {
		System.setOut(original);
	}

	private String run(String input) {
		new TelefonoMenu().iniciarMenu(adapter, new Scanner(input));
		return out.toString(StandardCharsets.UTF_8);
	}

	@Test
	void alternaMotoresEnLaMismaSesion() throws Exception {
		when(adapter.contar(DatabaseOption.MARIA)).thenReturn(3);
		when(adapter.contar(DatabaseOption.MONGO)).thenReturn(4);
		String salida = run("1\n3\n0\n2\n3\n0\n0\n");
		assertTrue(salida.contains("Total en MARIA: 3"), salida);
		assertTrue(salida.contains("Total en MONGO: 4"), salida);
	}

	@Test
	void creaConservandoLosCerosInicialesYUnOperadorDeVariasPalabras() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MONGO), any())).thenAnswer(i -> i.getArgument(1));
		String salida = run("2\n4\n0012345\nClaro Hogar\n123456789\n0\n0\n");
		verify(adapter).crear(DatabaseOption.MONGO, new TelefonoModelCli("0012345", "Claro Hogar", 123456789));
		assertTrue(salida.contains("Creado: Teléfono [número=0012345, operador=Claro Hogar, dueño=123456789]"), salida);
	}

	@Test
	void editarMuestraElActualYConservaElNumero() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, "3001234567"))
				.thenReturn(new TelefonoModelCli("3001234567", "Claro", 123456789));
		when(adapter.editar(eq(DatabaseOption.MARIA), eq("3001234567"), any())).thenAnswer(i -> i.getArgument(2));
		String salida = run("1\n5\n3001234567\nTigo\n321654987\n0\n0\n");
		verify(adapter).editar(DatabaseOption.MARIA, "3001234567", new TelefonoModelCli("3001234567", "Tigo", 321654987));
		assertTrue(salida.contains("Actual: Teléfono [número=3001234567, operador=Claro"), salida);
		assertTrue(salida.contains("Editado: Teléfono [número=3001234567, operador=Tigo, dueño=321654987]"), salida);
	}

	@Test
	void erroresMuestranCodigoYMotorYSigueLaSesion() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MARIA), any()))
				.thenThrow(new ReferenceNotFoundException("No existe una persona con cc 999 para ser dueña del teléfono"));
		when(adapter.consultar(DatabaseOption.MARIA, "3109999999"))
				.thenThrow(new NoExistException("No existe un teléfono con número 3109999999"));
		doThrow(new InvalidDataException("El número debe tener entre 1 y 15 dígitos")).when(adapter)
				.eliminar(DatabaseOption.MARIA, "abc");
		when(adapter.listarPagina(DatabaseOption.MARIA, 0, 2))
				.thenReturn(List.of(new TelefonoModelCli("3001234567", "Claro", 123456789)));
		String salida = run("1\n4\n3001234567\nClaro\n999\n1\n3109999999\n6\nabc\n7\n0\n2\n0\n0\n");
		assertTrue(salida.contains("[ERR-05] No existe una persona con cc 999 para ser dueña del teléfono en MARIA"),
				salida);
		assertTrue(salida.contains("[ERR-03] No existe un teléfono con número 3109999999 en MARIA"), salida);
		assertTrue(salida.contains("[ERR-01] El número debe tener entre 1 y 15 dígitos"), salida);
		assertTrue(salida.contains("Página 0 · 1 registros"), salida);
	}

	@Test
	void cedulaNoNumericaSeVuelveAPedir() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MARIA), any())).thenAnswer(i -> i.getArgument(1));
		String salida = run("1\n4\n3001234567\nClaro\nabc\n123456789\n0\n0\n");
		assertTrue(salida.contains("[ERR-01] 'abc' no es un número entero"), salida);
		verify(adapter).crear(DatabaseOption.MARIA, new TelefonoModelCli("3001234567", "Claro", 123456789));
	}

	@Test
	void motorInvalidoEsErr02() {
		assertTrue(run("5\n0\n").contains("[ERR-02]"));
	}
}
