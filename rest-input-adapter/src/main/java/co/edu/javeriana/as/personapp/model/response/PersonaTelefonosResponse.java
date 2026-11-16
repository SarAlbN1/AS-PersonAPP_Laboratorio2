package co.edu.javeriana.as.personapp.model.response;

import java.util.List;

/**
 * Persona con todos sus teléfonos (HU-033). Los campos de la persona tienen los
 * mismos nombres que {@code PersonaResponse}; {@code phones} va ordenada por número
 * como texto (C-06) y es una lista vacía cuando la persona no tiene teléfonos.
 */
public record PersonaTelefonosResponse(String dni, String firstName, String lastName, String age, String sex,
		List<TelefonoResponse> phones) {
}
