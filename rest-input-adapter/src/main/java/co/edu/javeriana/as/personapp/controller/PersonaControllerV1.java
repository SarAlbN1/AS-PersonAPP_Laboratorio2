package co.edu.javeriana.as.personapp.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.as.personapp.adapter.PersonaInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.PersonaRequest;
import co.edu.javeriana.as.personapp.model.response.PersonaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de Persona con las rutas comunes de la API REST §3.1. Todas exigen
 * {@code database=MARIA|MONGO} como parámetro de consulta (decisión D-04), que
 * sustituye la ruta {@code GET /api/v1/persona/{database}} de la plantilla, y
 * deja {@code /{cc}} libre para consultar, editar y eliminar. Los errores los
 * traduce {@link RestExceptionHandler}.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/persona", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Persona", description = "Seis operaciones y listado parcial sobre el motor indicado en database")
public class PersonaControllerV1 {

	@Autowired
	private PersonaInputAdapterRest personaInputAdapterRest;

	@Operation(summary = "Consultar una persona por su cc (C-10)")
	@GetMapping("/{cc}")
	public PersonaResponse consultar(@RequestParam String database, @PathVariable Integer cc) throws Exception {
		log.info("Into consultar persona REST API");
		return personaInputAdapterRest.consultar(database, cc);
	}

	@Operation(summary = "Listar todas las personas, ordenadas por cc (C-06)")
	@GetMapping
	public List<PersonaResponse> listar(@RequestParam String database) throws Exception {
		log.info("Into listar personas REST API");
		return personaInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar las personas del motor (C-08)")
	@GetMapping("/conteo")
	public Integer contar(@RequestParam String database) throws Exception {
		log.info("Into contar personas REST API");
		return personaInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de personas (C-07, adicional)")
	@GetMapping("/pagina")
	public List<PersonaResponse> listarPagina(@RequestParam String database,
			@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina personas REST API");
		return personaInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear una persona (C-02)")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PersonaResponse> crear(@RequestParam String database,
			@RequestBody PersonaRequest request) throws Exception {
		log.info("Into crear persona REST API");
		PersonaResponse creada = personaInputAdapterRest.crear(database, request);
		return ResponseEntity
				.created(URI.create("/api/v1/persona/" + creada.getDni() + "?database=" + database.toUpperCase()))
				.body(creada);
	}

	@Operation(summary = "Editar una persona (C-04)")
	@PutMapping(path = "/{cc}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public PersonaResponse editar(@RequestParam String database, @PathVariable Integer cc,
			@RequestBody PersonaRequest request) throws Exception {
		log.info("Into editar persona REST API");
		return personaInputAdapterRest.editar(database, cc, request);
	}

	@Operation(summary = "Eliminar una persona sin teléfonos ni estudios (C-05)")
	@DeleteMapping("/{cc}")
	public ResponseEntity<Void> eliminar(@RequestParam String database, @PathVariable Integer cc) throws Exception {
		log.info("Into eliminar persona REST API");
		personaInputAdapterRest.eliminar(database, cc);
		return ResponseEntity.noContent().build();
	}
}
