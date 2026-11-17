package co.edu.javeriana.as.personapp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PersonaRequest", description = "Persona que se envía al crear o editar. El motor no viaja en el cuerpo: va en el parámetro database (decisión D-04).")
public class PersonaRequest {

	@Schema(description = "Cédula: entero entre 1 y 2147483647. Es la identidad y no cambia al editar (C-04).", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
	private String dni;

	@Schema(description = "Nombre, de 1 a 45 caracteres tras recortar espacios.", example = "Pepe", requiredMode = Schema.RequiredMode.REQUIRED)
	private String firstName;

	@Schema(description = "Apellido, de 1 a 45 caracteres tras recortar espacios.", example = "Perez", requiredMode = Schema.RequiredMode.REQUIRED)
	private String lastName;

	@Schema(description = "Edad opcional; si viene, debe ser mayor o igual a 0.", example = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String age;

	@Schema(description = "Género: M o MALE, F o FEMALE. OTHER se rechaza con ERR-01, porque el ENUM de MariaDB solo admite M y F (decisión D-01).", example = "M", allowableValues = { "M", "F", "MALE", "FEMALE" }, requiredMode = Schema.RequiredMode.REQUIRED)
	private String sex;

	@Schema(description = "Campo heredado de la plantilla; se ignora en REST, donde el motor va en el parámetro database.", deprecated = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String database;
}
