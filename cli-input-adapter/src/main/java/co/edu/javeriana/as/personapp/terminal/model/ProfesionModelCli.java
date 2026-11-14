package co.edu.javeriana.as.personapp.terminal.model;

/** Profesión mostrada y capturada por la CLI, sin sus estudios (C-10). */
public record ProfesionModelCli(Integer id, String nombre, String descripcion) {

	@Override
	public String toString() {
		return "Profesión [id=" + id + ", nombre=" + nombre + ", descripción=" + descripcion + "]";
	}
}
