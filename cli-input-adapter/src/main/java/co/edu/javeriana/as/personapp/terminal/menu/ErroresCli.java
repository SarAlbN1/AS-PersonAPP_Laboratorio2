package co.edu.javeriana.as.personapp.terminal.menu;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.CannotCreateTransactionException;

import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;

/**
 * Formato común de error en la CLI (validaciones §2.2): {@code [ERR-xx] mensaje},
 * con los códigos del catálogo §3 y sin trazas.
 */
final class ErroresCli {

	private ErroresCli() {
	}

	static String formatear(Exception e, DatabaseOption database) {
		String motor = " en " + database;
		if (e instanceof InvalidDataException)
			return "[ERR-01] " + e.getMessage();
		if (e instanceof InvalidOptionException)
			return "[ERR-02] " + e.getMessage();
		if (e instanceof NoExistException)
			return "[ERR-03] " + e.getMessage() + motor;
		if (e instanceof DuplicateException)
			return "[ERR-04] " + e.getMessage() + motor;
		if (e instanceof DuplicateKeyException)
			return "[ERR-04] El registro ya existe" + motor;
		if (e instanceof DependentRecordsException)
			return "[ERR-06] " + e.getMessage() + motor;
		if (e instanceof KeyMismatchException)
			return "[ERR-09] " + e.getMessage();
		if (e instanceof DataAccessResourceFailureException || e instanceof CannotCreateTransactionException)
			return "[ERR-07] El motor no está disponible" + motor;
		return "[ERR-10] Error interno; la operación no se completó";
	}
}
