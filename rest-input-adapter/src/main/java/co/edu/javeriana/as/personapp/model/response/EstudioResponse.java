package co.edu.javeriana.as.personapp.model.response;

import java.time.LocalDate;

/** Estudio devuelto por REST, con las dos partes de su clave (C-10). */
public record EstudioResponse(Integer professionId, Integer personId, LocalDate graduationDate,
		String universityName) {
}
