package co.edu.javeriana.as.personapp.model.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de alta y edición de Estudios (API REST §3.1). La clave son los dos
 * enteros, en el orden de la ruta; la fecha va como {@code yyyy-MM-dd} y, como la
 * universidad, es opcional.
 */
@Schema(name = "EstudioRequest", description = "Estudio que se envía al crear o editar. La clave son los dos enteros y no cambia al editar (C-04). El motor va en el parámetro database (decisión D-04).")
public record EstudioRequest(
		@Schema(description = "Id de la profesión, entero mayor o igual a 1. Debe existir en el motor elegido; si no, ERR-05 (C-03).", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) Integer professionId,
		@Schema(description = "Cédula de la persona, entero mayor o igual a 1. Debe existir en el motor elegido; si no, ERR-05 (C-03).", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED) Integer personId,
		@Schema(description = "Fecha de grado opcional, en formato yyyy-MM-dd. Una fecha inválida da ERR-01.", example = "2020-11-30", requiredMode = Schema.RequiredMode.NOT_REQUIRED) LocalDate graduationDate,
		@Schema(description = "Universidad opcional, hasta 50 caracteres.", example = "Universidad de los Andes", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String universityName) {
}
