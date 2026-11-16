package co.edu.javeriana.as.personapp.terminal.menu;

import java.util.List;
import java.util.Scanner;

import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.PersonaInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.PersonaModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Menú de Persona con el menú común de la CLI (contrato CLI §3): primero el motor
 * y luego las operaciones. La numeración es la aprobada en HU-011: las tres
 * lecturas, las tres escrituras y el listado parcial como adicional, de modo que
 * la opción 1 pasa a ser consultar por identificador y ya no listar todos.
 * Volver con 0 permite cambiar de motor sin reiniciar la aplicación.
 */
@Slf4j
public class PersonaMenu {

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

	public void iniciarMenu(PersonaInputAdapterCli adapter, Scanner keyboard) {
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

	private void menuOperaciones(PersonaInputAdapterCli adapter, Scanner keyboard, DatabaseOption db) {
		while (true) {
			mostrarOperaciones(db);
			int opcion = ProfesionMenu.leerEntero(keyboard, "Ingrese una opción: ");
			if (opcion == REGRESAR)
				return;
			ejecutar(db, () -> {
				switch (opcion) {
				case CONSULTAR -> System.out.println(adapter.consultar(db, ProfesionMenu.leerEntero(keyboard, "Cédula: ")));
				case LISTAR -> imprimir(adapter.listar(db));
				case CONTAR -> System.out.println("Total en " + db + ": " + adapter.contar(db));
				case CREAR -> System.out.println("Creada: " + adapter.crear(db, capturar(keyboard, null)));
				case EDITAR -> {
					Integer cc = ProfesionMenu.leerEntero(keyboard, "Cédula: ");
					System.out.println("Actual: " + adapter.consultar(db, cc));
					System.out.println("Editada: " + adapter.editar(db, cc, capturar(keyboard, cc)));
				}
				case ELIMINAR -> {
					Integer cc = ProfesionMenu.leerEntero(keyboard, "Cédula: ");
					adapter.eliminar(db, cc);
					System.out.println("Eliminada la persona con cc " + cc + " en " + db);
				}
				case LISTAR_PARCIAL -> {
					Integer page = ProfesionMenu.leerEntero(keyboard, "Página (desde 0): ");
					Integer size = ProfesionMenu.leerEntero(keyboard, "Tamaño (1 a 100): ");
					List<PersonaModelCli> pagina = adapter.listarPagina(db, page, size);
					imprimir(pagina);
					System.out.println("Página " + page + " · " + pagina.size() + " registros");
				}
				default -> System.out.println("La opción elegida no es válida.");
				}
			});
		}
	}

	/** Captura una persona; con {@code cc} dado no se vuelve a pedir, porque la identidad no cambia (C-04). */
	private static PersonaModelCli capturar(Scanner keyboard, Integer cc) {
		Integer identificacion = cc != null ? cc : ProfesionMenu.leerEntero(keyboard, "Cédula: ");
		String nombre = ProfesionMenu.leerTexto(keyboard, "Nombre: ");
		String apellido = ProfesionMenu.leerTexto(keyboard, "Apellido: ");
		String genero = ProfesionMenu.leerTexto(keyboard, "Género (M o F): ");
		String edad = ProfesionMenu.leerTexto(keyboard, "Edad (vacío si no se conoce): ");
		return new PersonaModelCli(identificacion, nombre, apellido, genero,
				edad.isEmpty() ? null : Integer.valueOf(edad));
	}

	/** Muestra el error con su código y vuelve al menú sin cerrar la sesión. */
	private static void ejecutar(DatabaseOption db, Operacion operacion) {
		try {
			operacion.ejecutar();
		} catch (NumberFormatException e) {
			System.out.println("[ERR-01] La edad debe ser un número entero o quedar vacía");
		} catch (Exception e) {
			log.debug("Operación de Persona fallida", e);
			System.out.println(ErroresCli.formatear(e, db));
		}
	}

	private static void imprimir(List<PersonaModelCli> personas) {
		if (personas.isEmpty())
			System.out.println("(sin registros)");
		personas.forEach(System.out::println);
	}

	private static void mostrarOperaciones(DatabaseOption db) {
		System.out.println("---------------------- Persona · " + db);
		System.out.println(CONSULTAR + " Consultar por identificador · " + LISTAR + " Listar todos · " + CONTAR + " Contar");
		System.out.println(CREAR + " Crear · " + EDITAR + " Editar · " + ELIMINAR + " Eliminar");
		System.out.println(LISTAR_PARCIAL + " Listar parcial");
		System.out.println(REGRESAR + " Regresar al menú de motor");
	}
}
