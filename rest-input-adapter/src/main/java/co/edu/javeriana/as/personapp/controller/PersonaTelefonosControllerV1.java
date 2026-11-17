package co.edu.javeriana.as.personapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.as.personapp.adapter.PersonaTelefonosInputAdapterRest;
import co.edu.javeriana.as.personapp.model.response.PersonaTelefonosResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * Consulta adicional de HU-033: una persona con todos sus teléfonos. No sustituye
 * la consulta por identificador de Persona, que sigue sin colecciones (C-10). Va en
 * un controlador aparte para no tocar {@code PersonaControllerV1}; las rutas no se
 * cruzan porque esta termina en {@code /telefonos}.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/persona", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Persona con teléfonos", description = "Persona y todos sus teléfonos en el motor indicado en database")
public class PersonaTelefonosControllerV1 {

	@Autowired
	private PersonaTelefonosInputAdapterRest personaTelefonosInputAdapterRest;

	@Operation(summary = "Consultar una persona con todos sus teléfonos (HU-033)", description = "Devuelve la persona "
			+ "y sus teléfonos ordenados por número como texto; lista vacía si no tiene. 400 ERR-01 si cc no es un "
			+ "entero mayor o igual a 1, 400 ERR-02 si el motor no es MARIA ni MONGO y 404 ERR-03 si la persona no existe.")
	@GetMapping("/{cc}/telefonos")
	public PersonaTelefonosResponse consultarConTelefonos(@RequestParam String database, @PathVariable Integer cc)
			throws Exception {
		log.info("Into consultarConTelefonos persona REST API");
		return personaTelefonosInputAdapterRest.consultarConTelefonos(database, cc);
	}
}
