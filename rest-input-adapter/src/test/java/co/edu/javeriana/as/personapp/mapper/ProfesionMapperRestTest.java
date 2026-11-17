package co.edu.javeriana.as.personapp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.ProfessionUseCase;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.model.request.ProfesionRequest;

/** Un cuerpo sin id o sin nombre llega al caso de uso y da ERR-01, no un 500. */
class ProfesionMapperRestTest {

	private final ProfesionMapperRest mapper = new ProfesionMapperRest();

	/** El caso de uso rechaza antes de tocar la persistencia, así que el puerto no se usa. */
	private final ProfessionUseCase useCase = new ProfessionUseCase((ProfessionOutputPort) java.lang.reflect.Proxy
			.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ProfessionOutputPort.class }, (p, m, a) -> {
				throw new AssertionError("no debe llegar a la persistencia: " + m.getName());
			}));

	@Test
	void copiaLosCampos() {
		Profession profession = mapper.fromAdapterToDomain(new ProfesionRequest(7, "Ingeniería", "Sistemas"));
		assertEquals(7, profession.getIdentification());
		assertEquals("Ingeniería", profession.getName());
		assertEquals("Sistemas", profession.getDescription());
	}

	@Test
	void unCampoAusenteQuedaSinAsignar() {
		Profession profession = mapper.fromAdapterToDomain(new ProfesionRequest(null, null, null));
		assertNull(profession.getIdentification());
		assertNull(profession.getName());
		assertNull(profession.getDescription());
	}

	@Test
	void sinIdOSinNombreElCasoDeUsoDaErr01AlCrearYEditar() {
		for (ProfesionRequest body : List.of(new ProfesionRequest(null, "Ingeniería", null),
				new ProfesionRequest(7, null, null), new ProfesionRequest(null, null, null))) {
			assertThrows(InvalidDataException.class, () -> useCase.create(mapper.fromAdapterToDomain(body)));
			assertThrows(InvalidDataException.class, () -> useCase.edit(7, mapper.fromAdapterToDomain(body)));
		}
	}
}
