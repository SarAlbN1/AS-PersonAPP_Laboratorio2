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

import co.edu.javeriana.as.personapp.adapter.EstudioInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.EstudioRequest;
import co.edu.javeriana.as.personapp.model.response.ErrorResponse;
import co.edu.javeriana.as.personapp.model.response.EstudioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de Estudios con las rutas comunes de la API REST §3.1. La clave son dos
 * segmentos, {@code /{idProfesion}/{ccPersona}}; todas las rutas exigen
 * {@code database=MARIA|MONGO} y los errores los traduce {@link RestExceptionHandler}.
 * La documentación OpenAPI sigue el patrón de {@link PersonaControllerV1} (HU-028).
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/estudio", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Estudios", description = "Seis operaciones y listado parcial sobre el motor indicado en database")
public class EstudioControllerV1 {

	private static final String MOTOR = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.";
	private static final String PROFESION = "Id de la profesión, primera parte de la clave. Con una sola parte no hay consulta parcial.";
	private static final String PERSONA = "Cédula de la persona, segunda parte de la clave.";

	@Autowired
	private EstudioInputAdapterRest estudioInputAdapterRest;

	@Operation(summary = "Consultar un estudio por su clave completa: profesión y persona (C-10)", description = "Devuelve el estudio del motor elegido con las dos partes de su clave, sin los datos de la persona ni de la profesión.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Estudio encontrado"),
			@ApiResponse(responseCode = "400", description = "ERR-01 una parte de la clave no es numérica, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe un estudio con ese par en el motor elegido, aunque exista en el otro", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/{idProfesion}/{ccPersona}")
	public EstudioResponse consultar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = PROFESION, example = "1") @PathVariable Integer idProfesion,
			@Parameter(description = PERSONA, example = "123456789") @PathVariable Integer ccPersona) throws Exception {
		log.info("Into consultar estudio REST API");
		return estudioInputAdapterRest.consultar(database, idProfesion, ccPersona);
	}

	@Operation(summary = "Listar todos los estudios, ordenados por profesión y luego persona (C-06)", description = "El orden lo pide el motor sobre las dos partes de la clave. Una colección vacía devuelve un arreglo vacío.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Arreglo de estudios ordenado por profesión y persona"),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping
	public List<EstudioResponse> listar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database)
			throws Exception {
		log.info("Into listar estudios REST API");
		return estudioInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar los estudios del motor (C-08)", description = "Entero no negativo calculado por el motor; una colección vacía devuelve 0.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Total de estudios en el motor", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "integer", format = "int32", example = "2"))),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/conteo")
	public Integer contar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database)
			throws Exception {
		log.info("Into contar estudios REST API");
		return estudioInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de estudios (C-07, adicional)", description = "Página con el mismo orden de C-06, pedido al motor. Una página fuera de rango devuelve un arreglo vacío.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Página de estudios; vacía si queda fuera de rango"),
			@ApiResponse(responseCode = "400", description = "ERR-01 página negativa o tamaño fuera de 1 a 100, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/pagina")
	public List<EstudioResponse> listarPagina(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = "Página, desde 0", example = "0") @RequestParam(defaultValue = "0") Integer page,
			@Parameter(description = "Tamaño de página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina estudios REST API");
		return estudioInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear un estudio con persona y profesión existentes en el mismo motor (C-02, C-03)", description = "Rechaza el par repetido antes de comprobar las referencias. La persona y la profesión se buscan en el motor elegido. La cabecera Location apunta al registro creado.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Estudio creado; la cabecera Location apunta al registro"),
			@ApiResponse(responseCode = "400", description = "ERR-01 datos inválidos (una parte de la clave ausente, universidad de más de 50 caracteres, fecha que no es yyyy-MM-dd o JSON mal formado), o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "ERR-04 ya existe un estudio con ese par en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "422", description = "ERR-05 la persona o la profesión no existen en el motor elegido; no se guarda nada", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EstudioResponse> crear(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@RequestBody EstudioRequest request) throws Exception {
		log.info("Into crear estudio REST API");
		EstudioResponse created = estudioInputAdapterRest.crear(database, request);
		return ResponseEntity.created(URI.create("/api/v1/estudio/" + created.professionId() + "/"
				+ created.personId() + "?database=" + database.toUpperCase())).body(created);
	}

	@Operation(summary = "Editar la fecha y la universidad de un estudio; la clave no cambia (C-04)", description = "El par del cuerpo debe coincidir con el de la ruta. Solo se actualizan la fecha y la universidad.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Estudio actualizado"),
			@ApiResponse(responseCode = "400", description = "ERR-01 datos inválidos, ERR-02 motor ausente o desconocido, o ERR-09 el par del cuerpo no coincide con el de la ruta", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe un estudio con ese par en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PutMapping(path = "/{idProfesion}/{ccPersona}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public EstudioResponse editar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = PROFESION, example = "1") @PathVariable Integer idProfesion,
			@Parameter(description = PERSONA, example = "123456789") @PathVariable Integer ccPersona,
			@RequestBody EstudioRequest request) throws Exception {
		log.info("Into editar estudio REST API");
		return estudioInputAdapterRest.editar(database, idProfesion, ccPersona, request);
	}

	@Operation(summary = "Eliminar un estudio (C-05, sin restricción)", description = "Un estudio no tiene dependientes. Borrarlo no toca a la persona ni a la profesión.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Estudio eliminado; sin cuerpo"),
			@ApiResponse(responseCode = "400", description = "ERR-01 una parte de la clave no es numérica, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe un estudio con ese par en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@DeleteMapping("/{idProfesion}/{ccPersona}")
	public ResponseEntity<Void> eliminar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = PROFESION, example = "1") @PathVariable Integer idProfesion,
			@Parameter(description = PERSONA, example = "123456789") @PathVariable Integer ccPersona) throws Exception {
		log.info("Into eliminar estudio REST API");
		estudioInputAdapterRest.eliminar(database, idProfesion, ccPersona);
		return ResponseEntity.noContent().build();
	}
}
