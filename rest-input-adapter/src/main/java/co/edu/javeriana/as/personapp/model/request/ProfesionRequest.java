package co.edu.javeriana.as.personapp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

/** Cuerpo de alta y edición de Profesión (API REST §3.1). */
@Schema(name = "ProfesionRequest", description = "Profesión que se envía al crear o editar. El motor va en el parámetro database (D-04).")
public record ProfesionRequest(
		@Schema(description = "Identificador: entero entre 1 y 2147483647. Es la identidad y no cambia al editar (C-04).", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer id,
		@Schema(description = "Nombre, de 1 a 90 caracteres.", example = "Ingeniero de Sistemas", requiredMode = Schema.RequiredMode.REQUIRED) String name,
		@Schema(description = "Descripción opcional, sin límite.", example = "Diseño y construcción de software", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String description) {
}
