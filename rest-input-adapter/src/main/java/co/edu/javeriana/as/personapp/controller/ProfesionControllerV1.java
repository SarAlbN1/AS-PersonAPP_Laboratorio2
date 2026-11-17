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

import co.edu.javeriana.as.personapp.adapter.ProfesionInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.ProfesionRequest;
import co.edu.javeriana.as.personapp.model.response.ProfesionResponse;
import co.edu.javeriana.as.personapp.model.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de Profesión con las rutas comunes de la API REST §3.1. Todas exigen
 * {@code database=MARIA|MONGO}; los errores los traduce {@link RestExceptionHandler}.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/profesion", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profesión", description = "Seis operaciones y listado parcial sobre el motor indicado en database")
public class ProfesionControllerV1 {

	private static final String MOTOR = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.";
	private static final String IDENTIFICADOR = "Identificador de la profesión: entero mayor o igual a 1.";

	@Autowired
	private ProfesionInputAdapterRest profesionInputAdapterRest;

	@Operation(summary = "Consultar una profesión por su id (C-10)", description = "Devuelve la profesión del motor elegido, sin sus estudios.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Profesión encontrada"),
			@ApiResponse(responseCode = "400", description = "ERR-01 el id no es un entero válido, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe una profesión con ese id en el motor elegido, aunque exista en el otro", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/{id}")
	public ProfesionResponse consultar(@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = IDENTIFICADOR, example = "1") @PathVariable Integer id) throws Exception {
		log.info("Into consultar profesion REST API");
		return profesionInputAdapterRest.consultar(database, id);
	}

	@Operation(summary = "Listar todas las profesiones, ordenadas por id (C-06)", description = "El orden ascendente por id lo aplica el motor. Una colección vacía devuelve un arreglo vacío, no un error.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Arreglo de profesiones ordenado por id"),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping
	public List<ProfesionResponse> listar(@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database) throws Exception {
		log.info("Into listar profesiones REST API");
		return profesionInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar las profesiones del motor (C-08)", description = "Entero no negativo calculado por el motor; una colección vacía devuelve 0.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Total de profesiones en el motor", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "integer", format = "int32", example = "2"))),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/conteo")
	public Integer contar(@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database) throws Exception {
		log.info("Into contar profesiones REST API");
		return profesionInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de profesiones (C-07, adicional)", description = "Página con el mismo orden de C-06, pedido al motor. Una página fuera de rango devuelve un arreglo vacío.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Página de profesiones; vacía si queda fuera de rango"),
			@ApiResponse(responseCode = "400", description = "ERR-01 página negativa o tamaño fuera de 1 a 100, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/pagina")
	public List<ProfesionResponse> listarPagina(@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = "Página, desde 0", example = "0") @RequestParam(defaultValue = "0") Integer page,
			@Parameter(description = "Tamaño de página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina profesiones REST API");
		return profesionInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear una profesión (C-02)", description = "Rechaza el alta duplicada antes de guardar y devuelve la ubicación del registro creado.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Profesión creada; la cabecera Location apunta al registro"),
			@ApiResponse(responseCode = "400", description = "ERR-01 id o nombre ausentes o inválidos, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "ERR-04 ya existe una profesión con ese id en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProfesionResponse> crear(@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@RequestBody ProfesionRequest request) throws Exception {
		log.info("Into crear profesion REST API");
		ProfesionResponse created = profesionInputAdapterRest.crear(database, request);
		return ResponseEntity
				.created(URI.create("/api/v1/profesion/" + created.id() + "?database=" + database.toUpperCase()))
				.body(created);
	}

	@Operation(summary = "Editar una profesión (C-04)", description = "La identidad no cambia: el id del cuerpo debe coincidir con el de la ruta.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Profesión actualizada"),
			@ApiResponse(responseCode = "400", description = "ERR-01 datos inválidos, ERR-02 motor ausente o desconocido, o ERR-09 el id del cuerpo no coincide con el de la ruta", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe una profesión con ese id en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ProfesionResponse editar(@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = IDENTIFICADOR, example = "1") @PathVariable Integer id,
			@RequestBody ProfesionRequest request) throws Exception {
		log.info("Into editar profesion REST API");
		return profesionInputAdapterRest.editar(database, id, request);
	}

	@Operation(summary = "Eliminar una profesión sin estudios (C-05)", description = "Borrado restrictivo: una profesión con estudios asociados responde ERR-06.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Profesión eliminada; sin cuerpo"),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe una profesión con ese id en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "ERR-06 la profesión tiene estudios asociados", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = IDENTIFICADOR, example = "1") @PathVariable Integer id) throws Exception {
		log.info("Into eliminar profesion REST API");
		profesionInputAdapterRest.eliminar(database, id);
		return ResponseEntity.noContent().build();
	}
}
