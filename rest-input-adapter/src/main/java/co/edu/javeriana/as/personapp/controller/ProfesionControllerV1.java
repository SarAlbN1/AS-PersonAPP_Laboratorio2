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
import io.swagger.v3.oas.annotations.Operation;
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

	@Autowired
	private ProfesionInputAdapterRest profesionInputAdapterRest;

	@Operation(summary = "Consultar una profesión por su id (C-10)")
	@GetMapping("/{id}")
	public ProfesionResponse consultar(@RequestParam String database, @PathVariable Integer id) throws Exception {
		log.info("Into consultar profesion REST API");
		return profesionInputAdapterRest.consultar(database, id);
	}

	@Operation(summary = "Listar todas las profesiones, ordenadas por id (C-06)")
	@GetMapping
	public List<ProfesionResponse> listar(@RequestParam String database) throws Exception {
		log.info("Into listar profesiones REST API");
		return profesionInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar las profesiones del motor (C-08)")
	@GetMapping("/conteo")
	public Integer contar(@RequestParam String database) throws Exception {
		log.info("Into contar profesiones REST API");
		return profesionInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de profesiones (C-07, adicional)")
	@GetMapping("/pagina")
	public List<ProfesionResponse> listarPagina(@RequestParam String database,
			@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina profesiones REST API");
		return profesionInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear una profesión (C-02)")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProfesionResponse> crear(@RequestParam String database,
			@RequestBody ProfesionRequest request) throws Exception {
		log.info("Into crear profesion REST API");
		ProfesionResponse created = profesionInputAdapterRest.crear(database, request);
		return ResponseEntity
				.created(URI.create("/api/v1/profesion/" + created.id() + "?database=" + database.toUpperCase()))
				.body(created);
	}

	@Operation(summary = "Editar una profesión (C-04)")
	@PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ProfesionResponse editar(@RequestParam String database, @PathVariable Integer id,
			@RequestBody ProfesionRequest request) throws Exception {
		log.info("Into editar profesion REST API");
		return profesionInputAdapterRest.editar(database, id, request);
	}

	@Operation(summary = "Eliminar una profesión sin estudios (C-05)")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@RequestParam String database, @PathVariable Integer id) throws Exception {
		log.info("Into eliminar profesion REST API");
		profesionInputAdapterRest.eliminar(database, id);
		return ResponseEntity.noContent().build();
	}
}
