package co.edu.javeriana.as.personapp.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Teléfono devuelto por REST, con la cédula de su dueño (C-10). */
@Schema(name = "TelefonoResponse", description = "Teléfono del motor elegido, con la cédula de su dueño y sin los datos de la persona.")
public record TelefonoResponse(
		@Schema(description = "Número, como texto.", example = "3001234567") String number,
		@Schema(description = "Operador.", example = "Claro") String company,
		@Schema(description = "Cédula del dueño.", example = "123456789") Integer ownerId) {
}
