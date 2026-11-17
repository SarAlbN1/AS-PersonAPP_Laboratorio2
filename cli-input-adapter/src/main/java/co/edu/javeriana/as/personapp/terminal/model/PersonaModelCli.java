package co.edu.javeriana.as.personapp.terminal.model;

/** Persona mostrada y capturada por la CLI, sin sus teléfonos ni estudios (C-10). */
public record PersonaModelCli(Integer cc, String nombre, String apellido, String genero, Integer edad) {

	@Override
	public String toString() {
		return "Persona [cc=" + cc + ", nombre=" + nombre + ", apellido=" + apellido + ", género=" + genero
				+ ", edad=" + (edad == null ? "sin dato" : edad) + "]";
	}
}
