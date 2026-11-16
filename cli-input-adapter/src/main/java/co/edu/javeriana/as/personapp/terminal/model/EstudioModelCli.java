package co.edu.javeriana.as.personapp.terminal.model;

import java.time.LocalDate;

/** Estudio mostrado y capturado por la CLI, con las dos partes de su clave (C-10). */
public record EstudioModelCli(Integer idProfesion, Integer ccPersona, LocalDate fecha, String universidad) {

	@Override
	public String toString() {
		return "Estudio [profesión=" + idProfesion + ", persona=" + ccPersona + ", fecha=" + fecha + ", universidad="
				+ universidad + "]";
	}
}
