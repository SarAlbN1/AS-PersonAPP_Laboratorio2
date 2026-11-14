package co.edu.javeriana.as.personapp.terminal.menu;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.ProfesionInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.ProfesionModelCli;

/** HU-018: el menú de Profesión sigue el menú común y los errores no cierran la sesión. */
class ProfesionMenuTest {

	private final PrintStream original = System.out;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private ProfesionInputAdapterCli adapter;

	@BeforeEach
	void setUp() {
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
		adapter = mock(ProfesionInputAdapterCli.class);
	}

	@AfterEach
	void tearDown() {
		System.setOut(original);
	}

	private String run(String input) {
		new ProfesionMenu().iniciarMenu(adapter, new Scanner(input));
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
	void creaConNombreDeVariasPalabras() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MARIA), any())).thenAnswer(i -> i.getArgument(1));
		String salida = run("1\n4\n5\nIngeniero Civil\nObras\n0\n0\n");
		verify(adapter).crear(DatabaseOption.MARIA, new ProfesionModelCli(5, "Ingeniero Civil", "Obras"));
		assertTrue(salida.contains("Creada: Profesión [id=5, nombre=Ingeniero Civil"), salida);
	}

	@Test
	void entradaNoNumericaSeVuelveAPedir() throws Exception {
		when(adapter.consultar(DatabaseOption.MONGO, 1)).thenReturn(new ProfesionModelCli(1, "A", "d"));
		String salida = run("2\n1\nabc\n1\n0\n0\n");
		assertTrue(salida.contains("[ERR-01] 'abc' no es un número entero"), salida);
		assertTrue(salida.contains("Profesión [id=1, nombre=A"), salida);
	}

	@Test
	void erroresMuestranCodigoYMotorYSigueLaSesion() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, 9)).thenThrow(new NoExistException("No existe una profesión con id 9"));
		org.mockito.Mockito.doThrow(new DependentRecordsException("La profesión con id 1 tiene estudios asociados"))
				.when(adapter).eliminar(DatabaseOption.MARIA, 1);
		when(adapter.listarPagina(DatabaseOption.MARIA, 0, 2)).thenReturn(List.of(new ProfesionModelCli(1, "A", "")));
		String salida = run("1\n1\n9\n6\n1\n7\n0\n2\n0\n0\n");
		assertTrue(salida.contains("[ERR-03] No existe una profesión con id 9 en MARIA"), salida);
		assertTrue(salida.contains("[ERR-06] La profesión con id 1 tiene estudios asociados en MARIA"), salida);
		assertTrue(salida.contains("Página 0 · 1 registros"), salida);
	}

	@Test
	void motorInvalidoEsErr02() {
		assertTrue(run("5\n0\n").contains("[ERR-02]"));
	}
}
