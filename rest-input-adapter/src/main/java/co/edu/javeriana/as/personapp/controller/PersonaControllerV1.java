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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

	@Operation(summary = "Consultar una persona por su cc (C-10)", description = "Devuelve la persona del motor elegido. No incluye sus teléfonos ni estudios: esa consulta es HU-033.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Persona encontrada"),
			@ApiResponse(responseCode = "400", description = "ERR-01 cc con formato inválido, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe una persona con ese cc en el motor elegido, aunque exista en el otro", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))) })
	@GetMapping("/{cc}")
	public PersonaResponse consultar(@RequestParam @Parameter(description = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.", example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = "Cédula de la persona", example = "123456789") @PathVariable Integer cc)
			throws Exception {
		log.info("Into consultar persona REST API");
		return personaInputAdapterRest.consultar(database, cc);
	}

	@Operation(summary = "Listar todas las personas, ordenadas por cc (C-06)", description = "El orden ascendente por clave lo aplica el motor. Una colección vacía devuelve un arreglo vacío, no un error.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Arreglo de personas ordenado por cc"),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))) })
	@GetMapping
	public List<PersonaResponse> listar(@RequestParam @Parameter(description = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.", example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database) throws Exception {
		log.info("Into listar personas REST API");
		return personaInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar las personas del motor (C-08)", description = "Entero no negativo calculado por el motor; una colección vacía devuelve 0.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Total de personas en el motor", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "integer", format = "int32", example = "5"))),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))) })
	@GetMapping("/conteo")
	public Integer contar(@RequestParam @Parameter(description = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.", example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database) throws Exception {
		log.info("Into contar personas REST API");
		return personaInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de personas (C-07, adicional)", description = "Página con el mismo orden de C-06, pedido al motor. Una página fuera de rango devuelve un arreglo vacío.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Página de personas; vacía si queda fuera de rango"),
			@ApiResponse(responseCode = "400", description = "ERR-01 página negativa o tamaño fuera de 1 a 100, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))) })
	@GetMapping("/pagina")
	public List<PersonaResponse> listarPagina(@RequestParam @Parameter(description = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.", example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = "Página, desde 0", example = "0") @RequestParam(defaultValue = "0") Integer page,
			@Parameter(description = "Tamaño de página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina personas REST API");
		return personaInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear una persona (C-02)", description = "Rechaza el alta duplicada antes de guardar y devuelve la ubicación del registro creado.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Persona creada; la cabecera Location apunta al registro"),
			@ApiResponse(responseCode = "400", description = "ERR-01 datos inválidos, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "ERR-04 ya existe una persona con ese cc en el motor elegido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PersonaResponse> crear(@RequestParam @Parameter(description = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.", example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@RequestBody PersonaRequest request) throws Exception {
		log.info("Into crear persona REST API");
		PersonaResponse creada = personaInputAdapterRest.crear(database, request);
		return ResponseEntity
				.created(URI.create("/api/v1/persona/" + creada.getDni() + "?database=" + database.toUpperCase()))
				.body(creada);
	}

	@Operation(summary = "Editar una persona (C-04)", description = "La identidad no cambia: el cc del cuerpo debe coincidir con el de la ruta.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Persona actualizada"),
			@ApiResponse(responseCode = "400", description = "ERR-01 datos inválidos, ERR-02 motor ausente o desconocido, o ERR-09 el cc del cuerpo no coincide con el de la ruta", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe una persona con ese cc en el motor elegido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))) })
	@PutMapping(path = "/{cc}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public PersonaResponse editar(@RequestParam @Parameter(description = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.", example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = "Cédula de la persona", example = "123456789") @PathVariable Integer cc,
			@RequestBody PersonaRequest request) throws Exception {
		log.info("Into editar persona REST API");
		return personaInputAdapterRest.editar(database, cc, request);
	}

	@Operation(summary = "Eliminar una persona sin teléfonos ni estudios (C-05)", description = "Borrado restrictivo: una persona con teléfonos o estudios asociados responde ERR-06.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Persona eliminada; sin cuerpo"),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe una persona con ese cc en el motor elegido", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "ERR-06 la persona tiene teléfonos o estudios asociados", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = co.edu.javeriana.as.personapp.model.response.ErrorResponse.class))) })
	@DeleteMapping("/{cc}")
	public ResponseEntity<Void> eliminar(@RequestParam @Parameter(description = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.", example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = "Cédula de la persona", example = "123456789") @PathVariable Integer cc)
			throws Exception {
		log.info("Into eliminar persona REST API");
		personaInputAdapterRest.eliminar(database, cc);
		return ResponseEntity.noContent().build();
	}
}
