package co.edu.javeriana.as.personapp.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Profesión devuelta por REST, sin sus estudios (C-10). */
@Schema(name = "ProfesionResponse", description = "Profesión del motor elegido, sin sus estudios.")
public record ProfesionResponse(
		@Schema(description = "Identificador de la profesión.", example = "1") Integer id,
		@Schema(description = "Nombre de la profesión.", example = "Ingeniero de Sistemas") String name,
		@Schema(description = "Descripción de la profesión; puede ser nula.", example = "Diseño y construcción de software") String description) {
}
