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
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de Teléfono con las rutas comunes de la API REST §3.1. La clave es el
 * número como texto; todas las rutas exigen {@code database=MARIA|MONGO} y los
 * errores los traduce {@link RestExceptionHandler}.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/telefono", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Teléfono", description = "Seis operaciones y listado parcial sobre el motor indicado en database")
public class TelefonoControllerV1 {

	@Autowired
	private TelefonoInputAdapterRest telefonoInputAdapterRest;

	@Operation(summary = "Consultar un teléfono por su número (C-10)")
	@GetMapping("/{num}")
	public TelefonoResponse consultar(@RequestParam String database, @PathVariable String num) throws Exception {
		log.info("Into consultar telefono REST API");
		return telefonoInputAdapterRest.consultar(database, num);
	}

	@Operation(summary = "Listar todos los teléfonos, ordenados por número como texto (C-06)")
	@GetMapping
	public List<TelefonoResponse> listar(@RequestParam String database) throws Exception {
		log.info("Into listar telefonos REST API");
		return telefonoInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar los teléfonos del motor (C-08)")
	@GetMapping("/conteo")
	public Integer contar(@RequestParam String database) throws Exception {
		log.info("Into contar telefonos REST API");
		return telefonoInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de teléfonos (C-07, adicional)")
	@GetMapping("/pagina")
	public List<TelefonoResponse> listarPagina(@RequestParam String database,
			@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina telefonos REST API");
		return telefonoInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear un teléfono con un dueño existente en el mismo motor (C-02, C-03)")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TelefonoResponse> crear(@RequestParam String database,
			@RequestBody TelefonoRequest request) throws Exception {
		log.info("Into crear telefono REST API");
		TelefonoResponse created = telefonoInputAdapterRest.crear(database, request);
		return ResponseEntity
				.created(URI.create("/api/v1/telefono/" + created.number() + "?database=" + database.toUpperCase()))
				.body(created);
	}

	@Operation(summary = "Editar el operador o el dueño de un teléfono (C-04)")
	@PutMapping(path = "/{num}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public TelefonoResponse editar(@RequestParam String database, @PathVariable String num,
			@RequestBody TelefonoRequest request) throws Exception {
		log.info("Into editar telefono REST API");
		return telefonoInputAdapterRest.editar(database, num, request);
	}

	@Operation(summary = "Eliminar un teléfono (C-05, sin restricción)")
	@DeleteMapping("/{num}")
	public ResponseEntity<Void> eliminar(@RequestParam String database, @PathVariable String num) throws Exception {
		log.info("Into eliminar telefono REST API");
		telefonoInputAdapterRest.eliminar(database, num);
		return ResponseEntity.noContent().build();
	}
}
