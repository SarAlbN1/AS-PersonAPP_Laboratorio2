package co.edu.javeriana.as.personapp.common.exceptions;

/**
 * ERR-06: borrado rechazado porque el registro tiene dependientes (C-05).
 */
public class DependentRecordsException extends Exception {

	private static final long serialVersionUID = 1L;

	public DependentRecordsException(String mensaje) {
		super(mensaje);
	}

}
