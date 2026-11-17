package co.edu.javeriana.as.personapp.model.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/** Estudio devuelto por REST, con las dos partes de su clave (C-10). */
@Schema(name = "EstudioResponse", description = "Estudio del motor elegido, con las dos partes de su clave y sin los datos de la persona ni de la profesión.")
public record EstudioResponse(
		@Schema(description = "Id de la profesión.", example = "1") Integer professionId,
		@Schema(description = "Cédula de la persona.", example = "123456789") Integer personId,
		@Schema(description = "Fecha de grado, o null si no se registró.", example = "2015-12-01") LocalDate graduationDate,
		@Schema(description = "Universidad, o null si no se registró.", example = "Pontificia Universidad Javeriana") String universityName) {
}
