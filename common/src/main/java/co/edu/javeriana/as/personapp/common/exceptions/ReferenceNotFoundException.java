package co.edu.javeriana.as.personapp.common.exceptions;

/**
 * ERR-05: el cuerpo referencia un dueño, una persona o una profesión que no existe
 * en el motor elegido (C-03). Si lo que no existe es el registro de la ruta, es ERR-03.
 */
public class ReferenceNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReferenceNotFoundException(String mensaje) {
		super(mensaje);
	}

}
