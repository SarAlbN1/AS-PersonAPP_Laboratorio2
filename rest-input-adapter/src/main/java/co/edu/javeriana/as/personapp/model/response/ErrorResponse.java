package co.edu.javeriana.as.personapp.model.response;

/** Formato común de error (validaciones §2.2): código ERR-xx y mensaje sin trazas. */
public record ErrorResponse(String error, String message) {
}
