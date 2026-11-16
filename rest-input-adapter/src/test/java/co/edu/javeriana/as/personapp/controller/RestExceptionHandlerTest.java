package co.edu.javeriana.as.personapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

/** HU-018: cada excepción sale con el código HTTP y el ERR-xx del catálogo. */
class RestExceptionHandlerTest {

	private final RestExceptionHandler handler = new RestExceptionHandler();
	private HttpServletRequest request;

	@BeforeEach
	void setUp() {
		request = mock(HttpServletRequest.class);
		when(request.getParameter("database")).thenReturn("mongo");
	}

	private static void check(ResponseEntity<ErrorResponse> r, int status, String code, String message) {
		assertEquals(status, r.getStatusCode().value());
		assertEquals(code, r.getBody().error());
		assertEquals(message, r.getBody().message());
	}

	@Test
	void datosInvalidosSonErr01() {
		check(handler.invalidData(new InvalidDataException("nombre vacío")), 400, "ERR-01", "nombre vacío");
	}

	@Test
	void motorInvalidoOAusenteEsErr02() throws Exception {
		check(handler.invalidOption(new InvalidOptionException("motor X")), 400, "ERR-02", "motor X");
		check(handler.missingParameter(new MissingServletRequestParameterException("database", "String")), 400,
				"ERR-02", "Falta el parámetro database; use MARIA o MONGO");
		assertEquals("ERR-01",
				handler.missingParameter(new MissingServletRequestParameterException("page", "Integer")).getBody().error());
	}

	@Test
	void ausenciaDuplicadoYDependientesNombranElMotor() {
		check(handler.noExist(new NoExistException("No existe una profesión con id 9"), request), 404, "ERR-03",
				"No existe una profesión con id 9 en MONGO");
		check(handler.duplicate(new DuplicateException("Ya existe una profesión con id 1"), request), 409, "ERR-04",
				"Ya existe una profesión con id 1 en MONGO");
		check(handler.dependents(new DependentRecordsException("Tiene estudios"), request), 409, "ERR-06",
				"Tiene estudios en MONGO");
	}

	@Test
	void claveDistintaEsErr09() {
		check(handler.keyMismatch(new KeyMismatchException("ids distintos")), 400, "ERR-09", "ids distintos");
	}

	@Test
	void motorCaidoEsErr07YNoExponeLaCausa() {
		check(handler.engineDown(new DataAccessResourceFailureException("timeout mongodb://persona_db:secreto@..."),
				request), 503, "ERR-07", "El motor no está disponible en MONGO");
	}

	@Test
	void errorNoClasificadoEsErr10SinTraza() {
		check(handler.unexpected(new IllegalStateException("SQL interno")), 500, "ERR-10",
				"Error interno; la operación no se completó");
	}
}
