package co.edu.javeriana.as.personapp.terminal.menu;

import java.util.List;
import java.util.Scanner;

import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.TelefonoInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.TelefonoModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Menú de Teléfono con el menú común de la CLI (contrato CLI §3), igual que el de
 * Profesión: primero el motor y luego las operaciones. El número se lee como texto
 * para conservar los ceros iniciales.
 */
@Slf4j
public class TelefonoMenu {

	private static final int REGRESAR = 0;
	private static final int MARIADB = 1;
	private static final int MONGODB = 2;

	private static final int CONSULTAR = 1;
	private static final int LISTAR = 2;
	private static final int CONTAR = 3;
	private static final int CREAR = 4;
	private static final int EDITAR = 5;
	private static final int ELIMINAR = 6;
	private static final int LISTAR_PARCIAL = 7;

	@FunctionalInterface
	private interface Operacion {
		void ejecutar() throws Exception;
	}

	public void iniciarMenu(TelefonoInputAdapterCli adapter, Scanner keyboard) {
		while (true) {
			System.out.println("----------------------");
			System.out.println(MARIADB + " para MariaDB");
			System.out.println(MONGODB + " para MongoDB");
			System.out.println(REGRESAR + " para regresar");
			switch (ProfesionMenu.leerEntero(keyboard, "Ingrese una opción: ")) {
			case REGRESAR:
				return;
			case MARIADB:
				menuOperaciones(adapter, keyboard, DatabaseOption.MARIA);
				break;
			case MONGODB:
				menuOperaciones(adapter, keyboard, DatabaseOption.MONGO);
				break;
			default:
				System.out.println("[ERR-02] Opción de motor no válida; use 1 (MariaDB) o 2 (MongoDB)");
			}
		}
	}

	private void menuOperaciones(TelefonoInputAdapterCli adapter, Scanner keyboard, DatabaseOption db) {
		while (true) {
			mostrarOperaciones(db);
			int opcion = ProfesionMenu.leerEntero(keyboard, "Ingrese una opción: ");
			if (opcion == REGRESAR)
				return;
			ejecutar(db, () -> {
				switch (opcion) {
				case CONSULTAR -> System.out.println(adapter.consultar(db, leerNumero(keyboard)));
				case LISTAR -> imprimir(adapter.listar(db));
				case CONTAR -> System.out.println("Total en " + db + ": " + adapter.contar(db));
				case CREAR -> {
					String numero = leerNumero(keyboard);
					String operador = ProfesionMenu.leerTexto(keyboard, "Operador: ");
					Integer dueno = ProfesionMenu.leerEntero(keyboard, "Cédula del dueño: ");
					System.out.println("Creado: " + adapter.crear(db, new TelefonoModelCli(numero, operador, dueno)));
				}
				case EDITAR -> {
					String numero = leerNumero(keyboard);
					System.out.println("Actual: " + adapter.consultar(db, numero));
					String operador = ProfesionMenu.leerTexto(keyboard, "Nuevo operador: ");
					Integer dueno = ProfesionMenu.leerEntero(keyboard, "Cédula del nuevo dueño: ");
					System.out.println(
							"Editado: " + adapter.editar(db, numero, new TelefonoModelCli(numero, operador, dueno)));
				}
				case ELIMINAR -> {
					String numero = leerNumero(keyboard);
					adapter.eliminar(db, numero);
					System.out.println("Eliminado el teléfono " + numero + " en " + db);
				}
				case LISTAR_PARCIAL -> {
					Integer page = ProfesionMenu.leerEntero(keyboard, "Página (desde 0): ");
					Integer size = ProfesionMenu.leerEntero(keyboard, "Tamaño (1 a 100): ");
					List<TelefonoModelCli> pagina = adapter.listarPagina(db, page, size);
					imprimir(pagina);
					System.out.println("Página " + page + " · " + pagina.size() + " registros");
				}
				default -> System.out.println("La opción elegida no es válida.");
				}
			});
		}
	}

	/** El número se lee como texto: el caso de uso valida sus dígitos (ERR-01). */
	private static String leerNumero(Scanner keyboard) {
		return ProfesionMenu.leerTexto(keyboard, "Número: ");
	}

	/** Muestra el error con su código y vuelve al menú sin cerrar la sesión. */
	private static void ejecutar(DatabaseOption db, Operacion operacion) {
		try {
			operacion.ejecutar();
		} catch (Exception e) {
			log.debug("Operación de Teléfono fallida", e);
			System.out.println(ErroresCli.formatear(e, db));
		}
	}

	private static void imprimir(List<TelefonoModelCli> telefonos) {
		if (telefonos.isEmpty())
			System.out.println("(sin registros)");
		telefonos.forEach(System.out::println);
	}

	private static void mostrarOperaciones(DatabaseOption db) {
		System.out.println("---------------------- Teléfono · " + db);
		System.out.println(CONSULTAR + " Consultar por identificador · " + LISTAR + " Listar todos · " + CONTAR + " Contar");
		System.out.println(CREAR + " Crear · " + EDITAR + " Editar · " + ELIMINAR + " Eliminar");
		System.out.println(LISTAR_PARCIAL + " Listar parcial");
		System.out.println(REGRESAR + " Regresar al menú de motor");
	}
}
