package co.edu.javeriana.as.personapp.common.exceptions;

/**
 * ERR-04: alta con una clave que ya existe en el motor elegido (C-02).
 */
public class DuplicateException extends Exception {

	private static final long serialVersionUID = 1L;

	public DuplicateException(String mensaje) {
		super(mensaje);
	}

}
