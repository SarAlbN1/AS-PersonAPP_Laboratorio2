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

import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.PersonaInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.PersonaModelCli;
import co.edu.javeriana.as.personapp.terminal.model.PersonaTelefonosModelCli;
import co.edu.javeriana.as.personapp.terminal.model.TelefonoModelCli;

/**
 * HU-015: el menú de Persona sigue el menú común aprobado en HU-011, alterna
 * motores en la misma sesión y los errores no la cierran.
 */
class PersonaMenuTest {

	private final PrintStream original = System.out;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private PersonaInputAdapterCli adapter;

	@BeforeEach
	void setUp() {
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
		adapter = mock(PersonaInputAdapterCli.class);
	}

	@AfterEach
	void tearDown() {
		System.setOut(original);
	}

	private String run(String input) {
		new PersonaMenu().iniciarMenu(adapter, new Scanner(input));
		return out.toString(StandardCharsets.UTF_8);
	}

	@Test
	void muestraLasSeisOperacionesYElListadoParcial() {
		String salida = run("1\n0\n0\n");
		assertTrue(salida.contains("1 Consultar por identificador · 2 Listar todos · 3 Contar"), salida);
		assertTrue(salida.contains("4 Crear · 5 Editar · 6 Eliminar"), salida);
		assertTrue(salida.contains("7 Listar parcial"), salida);
	}

	@Test
	void alternaMotoresEnLaMismaSesionSinReiniciar() throws Exception {
		when(adapter.contar(DatabaseOption.MARIA)).thenReturn(5);
		when(adapter.contar(DatabaseOption.MONGO)).thenReturn(6);
		String salida = run("1\n3\n0\n2\n3\n0\n0\n");
		assertTrue(salida.contains("Total en MARIA: 5"), salida);
		assertTrue(salida.contains("Total en MONGO: 6"), salida);
	}

	@Test
	void consultarPorIdentificadorEsLaOpcionUno() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, 123456789))
				.thenReturn(new PersonaModelCli(123456789, "Pepe", "Perez", "MALE", 30));
		String salida = run("1\n1\n123456789\n0\n0\n");
		assertTrue(salida.contains("Persona [cc=123456789, nombre=Pepe"), salida);
	}

	@Test
	void creaConApellidoDeVariasPalabrasYEdadVacia() throws Exception {
		when(adapter.crear(eq(DatabaseOption.MONGO), any())).thenAnswer(i -> i.getArgument(1));
		String salida = run("2\n4\n991\nAna Maria\nPerez Gomez\nF\n\n0\n0\n");
		verify(adapter).crear(DatabaseOption.MONGO, new PersonaModelCli(991, "Ana Maria", "Perez Gomez", "F", null));
		assertTrue(salida.contains("edad=sin dato"), salida);
	}

	@Test
	void editarNoVuelveAPedirLaCedula() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, 5)).thenReturn(new PersonaModelCli(5, "Ana", "Perez", "FEMALE", 20));
		when(adapter.editar(eq(DatabaseOption.MARIA), eq(5), any())).thenAnswer(i -> i.getArgument(2));
		run("1\n5\n5\nAna Maria\nPerez\nF\n21\n0\n0\n");
		verify(adapter).editar(DatabaseOption.MARIA, 5, new PersonaModelCli(5, "Ana Maria", "Perez", "F", 21));
	}

	@Test
	void losErroresMuestranSuCodigoYLaSesionSigue() throws Exception {
		when(adapter.consultar(DatabaseOption.MARIA, 9)).thenThrow(new NoExistException("No existe una persona con cc 9"));
		when(adapter.crear(eq(DatabaseOption.MARIA), any()))
				.thenThrow(new DuplicateException("Ya existe una persona con cc 1"));
		when(adapter.listarPagina(DatabaseOption.MARIA, 0, 2))
				.thenReturn(List.of(new PersonaModelCli(1, "Ana", "Perez", "FEMALE", 20)));
		String salida = run("1\n1\n9\n4\n1\nAna\nPerez\nF\n20\n7\n0\n2\n0\n0\n");
		assertTrue(salida.contains("[ERR-03] No existe una persona con cc 9 en MARIA"), salida);
		assertTrue(salida.contains("[ERR-04] Ya existe una persona con cc 1 en MARIA"), salida);
		assertTrue(salida.contains("Página 0 · 1 registros"), salida);
	}

	@Test
	void laEdadNoNumericaEsErr01YNoRompeElMenu() throws Exception {
		String salida = run("1\n4\n7\nAna\nPerez\nF\nveinte\n0\n0\n");
		assertTrue(salida.contains("[ERR-01] La edad debe ser un número entero o quedar vacía"), salida);
	}

	@Test
	void eliminarSinRegistrosYListadoVacio() throws Exception {
		doThrow(new NoExistException("No existe una persona con cc 404")).when(adapter)
				.eliminar(DatabaseOption.MONGO, 404);
		when(adapter.listar(DatabaseOption.MONGO)).thenReturn(List.of());
		String salida = run("2\n6\n404\n2\n0\n0\n");
		assertTrue(salida.contains("[ERR-03] No existe una persona con cc 404 en MONGO"), salida);
		assertTrue(salida.contains("(sin registros)"), salida);
	}

	@Test
	void eliminarConDependientesMuestraErr06YLaSesionSigue() throws Exception {
		doThrow(new DependentRecordsException("La persona con cc 1 tiene teléfonos o estudios asociados"))
				.when(adapter).eliminar(DatabaseOption.MARIA, 1);
		when(adapter.contar(DatabaseOption.MARIA)).thenReturn(5);
		String salida = run("1\n6\n1\n3\n0\n0\n");
		assertTrue(salida.contains("[ERR-06] La persona con cc 1 tiene teléfonos o estudios asociados en MARIA"), salida);
		assertTrue(salida.contains("Total en MARIA: 5"), salida);
	}

	@Test
	void motorInvalidoEsErr02() {
		assertTrue(run("5\n0\n").contains("[ERR-02]"));
	}

	private static final PersonaModelCli PEPE = new PersonaModelCli(123456789, "Pepe", "Perez", "MALE", 30);

	@Test
	void consultaConTelefonosEsLaOpcionOchoYMuestraTodos() throws Exception {
		when(adapter.consultarConTelefonos(DatabaseOption.MONGO, 123456789))
				.thenReturn(new PersonaTelefonosModelCli(PEPE, List.of(new TelefonoModelCli("0012345", "Tigo", 123456789),
						new TelefonoModelCli("3001234567", "Claro", 123456789))));
		String salida = run("2\n8\n123456789\n0\n0\n");
		assertTrue(salida.contains("8 Consultar con sus teléfonos"), salida);
		assertTrue(salida.contains("Persona [cc=123456789, nombre=Pepe"), salida);
		assertTrue(salida.indexOf("número=0012345") < salida.indexOf("número=3001234567"), salida);
		assertTrue(salida.contains("Teléfonos en MONGO: 2"), salida);
	}

	@Test
	void personaSinTelefonosMuestraLaListaVacia() throws Exception {
		when(adapter.consultarConTelefonos(DatabaseOption.MARIA, 147258369)).thenReturn(new PersonaTelefonosModelCli(
				new PersonaModelCli(147258369, "Pepita", "Juarez", "FEMALE", 10), List.of()));
		String salida = run("1\n8\n147258369\n0\n0\n");
		assertTrue(salida.contains("(sin teléfonos)"), salida);
		assertTrue(salida.contains("Teléfonos en MARIA: 0"), salida);
	}

	@Test
	void conTelefonosInexistenteOInvalidaMuestraElErrorSinCerrarLaSesion() throws Exception {
		when(adapter.consultarConTelefonos(DatabaseOption.MARIA, 999))
				.thenThrow(new NoExistException("No existe una persona con cc 999"));
		when(adapter.consultarConTelefonos(DatabaseOption.MARIA, 0))
				.thenThrow(new InvalidDataException("La cédula debe ser un entero mayor o igual a 1"));
		when(adapter.contar(DatabaseOption.MARIA)).thenReturn(5);
		String salida = run("1\n8\n999\n8\n0\n3\n0\n0\n");
		assertTrue(salida.contains("[ERR-03] No existe una persona con cc 999 en MARIA"), salida);
		assertTrue(salida.contains("[ERR-01] La cédula debe ser un entero mayor o igual a 1"), salida);
		assertTrue(salida.contains("Total en MARIA: 5"), salida);
	}
}
