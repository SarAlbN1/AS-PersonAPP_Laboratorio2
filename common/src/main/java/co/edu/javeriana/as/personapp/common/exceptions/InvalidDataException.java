package co.edu.javeriana.as.personapp.common.exceptions;

/**
 * ERR-01: datos de entrada que no cumplen las validaciones comunes (HU-011).
 */
public class InvalidDataException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidDataException(String mensaje) {
		super(mensaje);
	}

}
