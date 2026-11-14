package co.edu.javeriana.as.personapp.common.exceptions;

/**
 * ERR-09: la clave de la ruta y la del cuerpo no coinciden al editar (C-04).
 */
public class KeyMismatchException extends Exception {

	private static final long serialVersionUID = 1L;

	public KeyMismatchException(String mensaje) {
		super(mensaje);
	}

}
