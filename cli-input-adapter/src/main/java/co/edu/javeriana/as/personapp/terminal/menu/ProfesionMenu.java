package co.edu.javeriana.as.personapp.terminal.menu;

import java.util.List;
import java.util.Scanner;

import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.ProfesionInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.ProfesionModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Menú de Profesión con el menú común de la CLI (contrato CLI §3): primero el
 * motor y luego las operaciones. Volver con 0 permite cambiar de motor sin
 * reiniciar la aplicación.
 */
@Slf4j
public class ProfesionMenu {

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

	public void iniciarMenu(ProfesionInputAdapterCli adapter, Scanner keyboard) {
		while (true) {
			System.out.println("----------------------");
			System.out.println(MARIADB + " para MariaDB");
			System.out.println(MONGODB + " para MongoDB");
			System.out.println(REGRESAR + " para regresar");
			switch (leerEntero(keyboard, "Ingrese una opción: ")) {
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

	private void menuOperaciones(ProfesionInputAdapterCli adapter, Scanner keyboard, DatabaseOption db) {
		while (true) {
			mostrarOperaciones(db);
			int opcion = leerEntero(keyboard, "Ingrese una opción: ");
			if (opcion == REGRESAR)
				return;
			ejecutar(db, () -> {
				switch (opcion) {
				case CONSULTAR -> System.out.println(adapter.consultar(db, leerEntero(keyboard, "Id: ")));
				case LISTAR -> imprimir(adapter.listar(db));
				case CONTAR -> System.out.println("Total en " + db + ": " + adapter.contar(db));
				case CREAR -> {
					Integer id = leerEntero(keyboard, "Id: ");
					String nombre = leerTexto(keyboard, "Nombre: ");
					String descripcion = leerTexto(keyboard, "Descripción: ");
					System.out.println("Creada: " + adapter.crear(db, new ProfesionModelCli(id, nombre, descripcion)));
				}
				case EDITAR -> {
					Integer id = leerEntero(keyboard, "Id: ");
					System.out.println("Actual: " + adapter.consultar(db, id));
					String nombre = leerTexto(keyboard, "Nuevo nombre: ");
					String descripcion = leerTexto(keyboard, "Nueva descripción: ");
					System.out.println("Editada: " + adapter.editar(db, id, new ProfesionModelCli(id, nombre, descripcion)));
				}
				case ELIMINAR -> {
					Integer id = leerEntero(keyboard, "Id: ");
					adapter.eliminar(db, id);
					System.out.println("Eliminada la profesión con id " + id + " en " + db);
				}
				case LISTAR_PARCIAL -> {
					Integer page = leerEntero(keyboard, "Página (desde 0): ");
					Integer size = leerEntero(keyboard, "Tamaño (1 a 100): ");
					List<ProfesionModelCli> pagina = adapter.listarPagina(db, page, size);
					imprimir(pagina);
					System.out.println("Página " + page + " · " + pagina.size() + " registros");
				}
				default -> System.out.println("La opción elegida no es válida.");
				}
			});
		}
	}

	/** Muestra el error con su código y vuelve al menú sin cerrar la sesión. */
	private static void ejecutar(DatabaseOption db, Operacion operacion) {
		try {
			operacion.ejecutar();
		} catch (Exception e) {
			log.debug("Operación de Profesión fallida", e);
			System.out.println(ErroresCli.formatear(e, db));
		}
	}

	private static void imprimir(List<ProfesionModelCli> profesiones) {
		if (profesiones.isEmpty())
			System.out.println("(sin registros)");
		profesiones.forEach(System.out::println);
	}

	private static void mostrarOperaciones(DatabaseOption db) {
		System.out.println("---------------------- Profesión · " + db);
		System.out.println(CONSULTAR + " Consultar por identificador · " + LISTAR + " Listar todos · " + CONTAR + " Contar");
		System.out.println(CREAR + " Crear · " + EDITAR + " Editar · " + ELIMINAR + " Eliminar");
		System.out.println(LISTAR_PARCIAL + " Listar parcial");
		System.out.println(REGRESAR + " Regresar al menú de motor");
	}

	/** Descarta la entrada no numérica y vuelve a pedirla (ERR-01). */
	static int leerEntero(Scanner keyboard, String mensaje) {
		while (true) {
			System.out.print(mensaje);
			if (keyboard.hasNextInt()) {
				int valor = keyboard.nextInt();
				keyboard.nextLine();
				return valor;
			}
			System.out.println("[ERR-01] '" + keyboard.nextLine().trim() + "' no es un número entero");
		}
	}

	static String leerTexto(Scanner keyboard, String mensaje) {
		System.out.print(mensaje);
		return keyboard.nextLine().trim();
	}
}
