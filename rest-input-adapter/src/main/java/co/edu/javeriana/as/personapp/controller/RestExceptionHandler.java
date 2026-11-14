package co.edu.javeriana.as.personapp.controller;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Traduce las excepciones al formato común de error (validaciones §2.2) con
 * los códigos del catálogo §3. No expone trazas, SQL ni credenciales.
 *
 * Por ahora solo atiende a {@link ProfesionControllerV1} (HU-018), para no
 * cambiar las respuestas de Persona antes de HU-014; los demás recursos se
 * agregan en {@code assignableTypes} al exponerlos.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = ProfesionControllerV1.class)
public class RestExceptionHandler {

	private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(new ErrorResponse(code, message));
	}

	/** Agrega el motor a los mensajes del caso de uso, que no lo conoce. */
	private static String enMotor(String message, HttpServletRequest request) {
		String database = request.getParameter("database");
		return database == null ? message : message + " en " + database.toUpperCase();
	}

	@ExceptionHandler(InvalidDataException.class)
	ResponseEntity<ErrorResponse> invalidData(InvalidDataException e) {
		return error(HttpStatus.BAD_REQUEST, "ERR-01", e.getMessage());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ErrorResponse> typeMismatch(MethodArgumentTypeMismatchException e) {
		return error(HttpStatus.BAD_REQUEST, "ERR-01", "El valor '" + e.getValue() + "' no es válido para " + e.getName());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e) {
		return error(HttpStatus.BAD_REQUEST, "ERR-01", "El cuerpo no es un JSON válido o tiene tipos incorrectos");
	}

	@ExceptionHandler(InvalidOptionException.class)
	ResponseEntity<ErrorResponse> invalidOption(InvalidOptionException e) {
		return error(HttpStatus.BAD_REQUEST, "ERR-02", e.getMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<ErrorResponse> missingParameter(MissingServletRequestParameterException e) {
		if ("database".equals(e.getParameterName()))
			return error(HttpStatus.BAD_REQUEST, "ERR-02", "Falta el parámetro database; use MARIA o MONGO");
		return error(HttpStatus.BAD_REQUEST, "ERR-01", "Falta el parámetro " + e.getParameterName());
	}

	@ExceptionHandler(NoExistException.class)
	ResponseEntity<ErrorResponse> noExist(NoExistException e, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "ERR-03", enMotor(e.getMessage(), request));
	}

	@ExceptionHandler({ DuplicateException.class, DuplicateKeyException.class })
	ResponseEntity<ErrorResponse> duplicate(Exception e, HttpServletRequest request) {
		String message = e instanceof DuplicateException ? e.getMessage() : "El registro ya existe";
		return error(HttpStatus.CONFLICT, "ERR-04", enMotor(message, request));
	}

	@ExceptionHandler(DependentRecordsException.class)
	ResponseEntity<ErrorResponse> dependents(DependentRecordsException e, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, "ERR-06", enMotor(e.getMessage(), request));
	}

	@ExceptionHandler(KeyMismatchException.class)
	ResponseEntity<ErrorResponse> keyMismatch(KeyMismatchException e) {
		return error(HttpStatus.BAD_REQUEST, "ERR-09", e.getMessage());
	}

	@ExceptionHandler({ DataAccessResourceFailureException.class, CannotCreateTransactionException.class })
	ResponseEntity<ErrorResponse> engineDown(Exception e, HttpServletRequest request) {
		log.error("Motor no disponible: {}", e.getClass().getSimpleName());
		return error(HttpStatus.SERVICE_UNAVAILABLE, "ERR-07", enMotor("El motor no está disponible", request));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> unexpected(Exception e) {
		log.error("Error no clasificado en REST", e);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERR-10", "Error interno; la operación no se completó");
	}
}
