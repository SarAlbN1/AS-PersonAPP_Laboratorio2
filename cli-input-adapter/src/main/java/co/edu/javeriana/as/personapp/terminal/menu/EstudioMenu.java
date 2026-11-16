package co.edu.javeriana.as.personapp.terminal.menu;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.adapter.EstudioInputAdapterCli;
import co.edu.javeriana.as.personapp.terminal.model.EstudioModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Menú de Estudios con el menú común de la CLI (contrato CLI §3), igual que el de
 * Teléfono: primero el motor y luego las operaciones. La clave se pide en dos partes,
 * primero la profesión y luego la persona. La fecha y la universidad son opcionales:
 * con Enter quedan vacías.
 */
@Slf4j
public class EstudioMenu {

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

	public void iniciarMenu(EstudioInputAdapterCli adapter, Scanner keyboard) {
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

	private void menuOperaciones(EstudioInputAdapterCli adapter, Scanner keyboard, DatabaseOption db) {
		while (true) {
			mostrarOperaciones(db);
			int opcion = ProfesionMenu.leerEntero(keyboard, "Ingrese una opción: ");
			if (opcion == REGRESAR)
				return;
			ejecutar(db, () -> {
				switch (opcion) {
				case CONSULTAR -> {
					int idProfesion = leerProfesion(keyboard);
					int ccPersona = leerPersona(keyboard);
					System.out.println(adapter.consultar(db, idProfesion, ccPersona));
				}
				case LISTAR -> imprimir(adapter.listar(db));
				case CONTAR -> System.out.println("Total en " + db + ": " + adapter.contar(db));
				case CREAR -> {
					int idProfesion = leerProfesion(keyboard);
					int ccPersona = leerPersona(keyboard);
					LocalDate fecha = leerFecha(keyboard, "Fecha de grado (yyyy-MM-dd, Enter para omitir): ");
					String universidad = leerOpcional(keyboard, "Universidad (Enter para omitir): ");
					System.out.println("Creado: "
							+ adapter.crear(db, new EstudioModelCli(idProfesion, ccPersona, fecha, universidad)));
				}
				case EDITAR -> {
					int idProfesion = leerProfesion(keyboard);
					int ccPersona = leerPersona(keyboard);
					System.out.println("Actual: " + adapter.consultar(db, idProfesion, ccPersona));
					LocalDate fecha = leerFecha(keyboard, "Nueva fecha de grado (yyyy-MM-dd, Enter para dejarla vacía): ");
					String universidad = leerOpcional(keyboard, "Nueva universidad (Enter para dejarla vacía): ");
					System.out.println("Editado: " + adapter.editar(db, idProfesion, ccPersona,
							new EstudioModelCli(idProfesion, ccPersona, fecha, universidad)));
				}
				case ELIMINAR -> {
					int idProfesion = leerProfesion(keyboard);
					int ccPersona = leerPersona(keyboard);
					adapter.eliminar(db, idProfesion, ccPersona);
					System.out.println("Eliminado el estudio (" + idProfesion + ", " + ccPersona + ") en " + db);
				}
				case LISTAR_PARCIAL -> {
					Integer page = ProfesionMenu.leerEntero(keyboard, "Página (desde 0): ");
					Integer size = ProfesionMenu.leerEntero(keyboard, "Tamaño (1 a 100): ");
					List<EstudioModelCli> pagina = adapter.listarPagina(db, page, size);
					imprimir(pagina);
					System.out.println("Página " + page + " · " + pagina.size() + " registros");
				}
				default -> System.out.println("La opción elegida no es válida.");
				}
			});
		}
	}

	private static int leerProfesion(Scanner keyboard) {
		return ProfesionMenu.leerEntero(keyboard, "Id de la profesión: ");
	}

	private static int leerPersona(Scanner keyboard) {
		return ProfesionMenu.leerEntero(keyboard, "Cédula de la persona: ");
	}

	/** Fecha opcional en formato ISO; una fecha inválida se descarta y se vuelve a pedir (ERR-01). */
	static LocalDate leerFecha(Scanner keyboard, String mensaje) {
		while (true) {
			String texto = ProfesionMenu.leerTexto(keyboard, mensaje);
			if (texto.isEmpty())
				return null;
			try {
				return LocalDate.parse(texto);
			} catch (DateTimeParseException e) {
				System.out.println("[ERR-01] La fecha '" + texto + "' no es válida; use el formato yyyy-MM-dd");
			}
		}
	}

	private static String leerOpcional(Scanner keyboard, String mensaje) {
		String texto = ProfesionMenu.leerTexto(keyboard, mensaje);
		return texto.isEmpty() ? null : texto;
	}

	/** Muestra el error con su código y vuelve al menú sin cerrar la sesión. */
	private static void ejecutar(DatabaseOption db, Operacion operacion) {
		try {
			operacion.ejecutar();
		} catch (Exception e) {
			log.debug("Operación de Estudios fallida", e);
			System.out.println(ErroresCli.formatear(e, db));
		}
	}

	private static void imprimir(List<EstudioModelCli> estudios) {
		if (estudios.isEmpty())
			System.out.println("(sin registros)");
		estudios.forEach(System.out::println);
	}

	private static void mostrarOperaciones(DatabaseOption db) {
		System.out.println("---------------------- Estudios · " + db);
		System.out.println(CONSULTAR + " Consultar por identificador · " + LISTAR + " Listar todos · " + CONTAR + " Contar");
		System.out.println(CREAR + " Crear · " + EDITAR + " Editar · " + ELIMINAR + " Eliminar");
		System.out.println(LISTAR_PARCIAL + " Listar parcial");
		System.out.println(REGRESAR + " Regresar al menú de motor");
	}
}
