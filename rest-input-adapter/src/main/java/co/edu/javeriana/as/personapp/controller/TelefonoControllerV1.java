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

import co.edu.javeriana.as.personapp.adapter.TelefonoInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.TelefonoRequest;
import co.edu.javeriana.as.personapp.model.response.ErrorResponse;
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de Teléfono con las rutas comunes de la API REST §3.1. La clave es el
 * número como texto; todas las rutas exigen {@code database=MARIA|MONGO} y los
 * errores los traduce {@link RestExceptionHandler}. La documentación OpenAPI sigue
 * el patrón de {@link PersonaControllerV1} (HU-028).
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/telefono", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Teléfono", description = "Seis operaciones y listado parcial sobre el motor indicado en database")
public class TelefonoControllerV1 {

	private static final String MOTOR = "Motor de persistencia; obligatorio y sin valor por defecto (C-09). ERR-02 si falta o no es válido.";
	private static final String NUMERO = "Número del teléfono, de 1 a 15 dígitos. Es texto: conserva los ceros iniciales (C-01).";

	@Autowired
	private TelefonoInputAdapterRest telefonoInputAdapterRest;

	@Operation(summary = "Consultar un teléfono por su número (C-10)", description = "Devuelve el teléfono del motor elegido con la cédula de su dueño, sin los datos de la persona.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Teléfono encontrado"),
			@ApiResponse(responseCode = "400", description = "ERR-01 el número no tiene entre 1 y 15 dígitos, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe un teléfono con ese número en el motor elegido, aunque exista en el otro", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/{num}")
	public TelefonoResponse consultar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = NUMERO, schema = @Schema(type = "string", example = "3001234567")) @PathVariable String num) throws Exception {
		log.info("Into consultar telefono REST API");
		return telefonoInputAdapterRest.consultar(database, num);
	}

	@Operation(summary = "Listar todos los teléfonos, ordenados por número como texto (C-06)", description = "El motor ordena el número como texto, así que 0012345 va antes que 3001234567. Una colección vacía devuelve un arreglo vacío.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Arreglo de teléfonos ordenado por número"),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping
	public List<TelefonoResponse> listar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database)
			throws Exception {
		log.info("Into listar telefonos REST API");
		return telefonoInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar los teléfonos del motor (C-08)", description = "Entero no negativo calculado por el motor; una colección vacía devuelve 0.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Total de teléfonos en el motor", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "integer", format = "int32", example = "3"))),
			@ApiResponse(responseCode = "400", description = "ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/conteo")
	public Integer contar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database)
			throws Exception {
		log.info("Into contar telefonos REST API");
		return telefonoInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de teléfonos (C-07, adicional)", description = "Página con el mismo orden de C-06, pedido al motor. Una página fuera de rango devuelve un arreglo vacío.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Página de teléfonos; vacía si queda fuera de rango"),
			@ApiResponse(responseCode = "400", description = "ERR-01 página negativa o tamaño fuera de 1 a 100, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/pagina")
	public List<TelefonoResponse> listarPagina(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = "Página, desde 0", example = "0") @RequestParam(defaultValue = "0") Integer page,
			@Parameter(description = "Tamaño de página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina telefonos REST API");
		return telefonoInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear un teléfono con un dueño existente en el mismo motor (C-02, C-03)", description = "Rechaza el número repetido antes de guardar. El dueño se busca en el motor elegido. La cabecera Location apunta al registro creado.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Teléfono creado; la cabecera Location apunta al registro"),
			@ApiResponse(responseCode = "400", description = "ERR-01 datos inválidos (número, operador o dueño ausentes o con formato inválido, o JSON mal formado), o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "ERR-04 ya existe un teléfono con ese número en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "422", description = "ERR-05 no existe una persona con la cédula ownerId en el motor elegido; no se guarda nada", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TelefonoResponse> crear(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@RequestBody TelefonoRequest request) throws Exception {
		log.info("Into crear telefono REST API");
		TelefonoResponse created = telefonoInputAdapterRest.crear(database, request);
		return ResponseEntity
				.created(URI.create("/api/v1/telefono/" + created.number() + "?database=" + database.toUpperCase()))
				.body(created);
	}

	@Operation(summary = "Editar el operador o el dueño de un teléfono (C-04)", description = "El número no cambia: el del cuerpo debe coincidir con el de la ruta. El nuevo dueño debe existir en el motor elegido.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Teléfono actualizado"),
			@ApiResponse(responseCode = "400", description = "ERR-01 datos inválidos, ERR-02 motor ausente o desconocido, o ERR-09 el número del cuerpo no coincide con el de la ruta", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe un teléfono con ese número en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "422", description = "ERR-05 el nuevo dueño no existe en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PutMapping(path = "/{num}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public TelefonoResponse editar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = NUMERO, schema = @Schema(type = "string", example = "3001234567")) @PathVariable String num,
			@RequestBody TelefonoRequest request) throws Exception {
		log.info("Into editar telefono REST API");
		return telefonoInputAdapterRest.editar(database, num, request);
	}

	@Operation(summary = "Eliminar un teléfono (C-05, sin restricción)", description = "Un teléfono no tiene dependientes. Borrarlo no toca a su dueño.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Teléfono eliminado; sin cuerpo"),
			@ApiResponse(responseCode = "400", description = "ERR-01 el número no tiene entre 1 y 15 dígitos, o ERR-02 motor ausente o desconocido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "ERR-03 no existe un teléfono con ese número en el motor elegido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "ERR-07 el motor no está disponible", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@DeleteMapping("/{num}")
	public ResponseEntity<Void> eliminar(
			@RequestParam @Parameter(description = MOTOR, example = "MARIA", schema = @Schema(allowableValues = { "MARIA", "MONGO" })) String database,
			@Parameter(description = NUMERO, schema = @Schema(type = "string", example = "3001234567")) @PathVariable String num) throws Exception {
		log.info("Into eliminar telefono REST API");
		telefonoInputAdapterRest.eliminar(database, num);
		return ResponseEntity.noContent().build();
	}
}
