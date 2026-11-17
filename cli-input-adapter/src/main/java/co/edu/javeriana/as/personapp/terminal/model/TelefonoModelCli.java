package co.edu.javeriana.as.personapp.terminal.model;

/** Teléfono mostrado y capturado por la CLI, con la cédula de su dueño (C-10). */
public record TelefonoModelCli(String numero, String operador, Integer cedulaDueno) {

	@Override
	public String toString() {
		return "Teléfono [número=" + numero + ", operador=" + operador + ", dueño=" + cedulaDueno + "]";
	}
}
