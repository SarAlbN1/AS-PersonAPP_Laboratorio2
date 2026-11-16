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
import co.edu.javeriana.as.personapp.model.response.EstudioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de Estudios con las rutas comunes de la API REST §3.1. La clave son dos
 * segmentos, {@code /{idProfesion}/{ccPersona}}; todas las rutas exigen
 * {@code database=MARIA|MONGO} y los errores los traduce {@link RestExceptionHandler}.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/estudio", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Estudios", description = "Seis operaciones y listado parcial sobre el motor indicado en database")
public class EstudioControllerV1 {

	@Autowired
	private EstudioInputAdapterRest estudioInputAdapterRest;

	@Operation(summary = "Consultar un estudio por su clave completa: profesión y persona (C-10)")
	@GetMapping("/{idProfesion}/{ccPersona}")
	public EstudioResponse consultar(@RequestParam String database, @PathVariable Integer idProfesion,
			@PathVariable Integer ccPersona) throws Exception {
		log.info("Into consultar estudio REST API");
		return estudioInputAdapterRest.consultar(database, idProfesion, ccPersona);
	}

	@Operation(summary = "Listar todos los estudios, ordenados por profesión y luego persona (C-06)")
	@GetMapping
	public List<EstudioResponse> listar(@RequestParam String database) throws Exception {
		log.info("Into listar estudios REST API");
		return estudioInputAdapterRest.listar(database);
	}

	@Operation(summary = "Contar los estudios del motor (C-08)")
	@GetMapping("/conteo")
	public Integer contar(@RequestParam String database) throws Exception {
		log.info("Into contar estudios REST API");
		return estudioInputAdapterRest.contar(database);
	}

	@Operation(summary = "Listar una página de estudios (C-07, adicional)")
	@GetMapping("/pagina")
	public List<EstudioResponse> listarPagina(@RequestParam String database,
			@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size)
			throws Exception {
		log.info("Into listarPagina estudios REST API");
		return estudioInputAdapterRest.listarPagina(database, page, size);
	}

	@Operation(summary = "Crear un estudio con persona y profesión existentes en el mismo motor (C-02, C-03)")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EstudioResponse> crear(@RequestParam String database, @RequestBody EstudioRequest request)
			throws Exception {
		log.info("Into crear estudio REST API");
		EstudioResponse created = estudioInputAdapterRest.crear(database, request);
		return ResponseEntity.created(URI.create("/api/v1/estudio/" + created.professionId() + "/"
				+ created.personId() + "?database=" + database.toUpperCase())).body(created);
	}

	@Operation(summary = "Editar la fecha y la universidad de un estudio; la clave no cambia (C-04)")
	@PutMapping(path = "/{idProfesion}/{ccPersona}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public EstudioResponse editar(@RequestParam String database, @PathVariable Integer idProfesion,
			@PathVariable Integer ccPersona, @RequestBody EstudioRequest request) throws Exception {
		log.info("Into editar estudio REST API");
		return estudioInputAdapterRest.editar(database, idProfesion, ccPersona, request);
	}

	@Operation(summary = "Eliminar un estudio (C-05, sin restricción)")
	@DeleteMapping("/{idProfesion}/{ccPersona}")
	public ResponseEntity<Void> eliminar(@RequestParam String database, @PathVariable Integer idProfesion,
			@PathVariable Integer ccPersona) throws Exception {
		log.info("Into eliminar estudio REST API");
		estudioInputAdapterRest.eliminar(database, idProfesion, ccPersona);
		return ResponseEntity.noContent().build();
	}
}
