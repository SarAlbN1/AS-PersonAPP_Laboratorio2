package co.edu.javeriana.as.personapp.model.request;

import java.time.LocalDate;

/**
 * Cuerpo de alta y edición de Estudios (API REST §3.1). La clave son los dos
 * enteros, en el orden de la ruta; la fecha va como {@code yyyy-MM-dd} y, como la
 * universidad, es opcional.
 */
public record EstudioRequest(Integer professionId, Integer personId, LocalDate graduationDate,
		String universityName) {
}
