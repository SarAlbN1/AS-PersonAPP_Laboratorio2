package co.edu.javeriana.as.personapp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de alta y edición de Teléfono (API REST §3.1). El número es texto para
 * conservar los ceros iniciales; el dueño se indica con su cédula.
 */
@Schema(name = "TelefonoRequest", description = "Teléfono que se envía al crear o editar. El motor no viaja en el cuerpo: va en el parámetro database (decisión D-04).")
public record TelefonoRequest(
		@Schema(description = "Número de 1 a 15 dígitos, como texto para conservar los ceros iniciales. Es la identidad y no cambia al editar (C-04).", example = "3001234567", requiredMode = Schema.RequiredMode.REQUIRED) String number,
		@Schema(description = "Operador, de 1 a 45 caracteres.", example = "Claro", requiredMode = Schema.RequiredMode.REQUIRED) String company,
		@Schema(description = "Cédula del dueño. Debe existir en el motor elegido; si no, ERR-05 (C-03).", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED) Integer ownerId) {
}
