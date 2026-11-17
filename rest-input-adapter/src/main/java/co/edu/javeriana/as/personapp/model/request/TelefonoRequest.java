package co.edu.javeriana.as.personapp.model.request;

/**
 * Cuerpo de alta y edición de Teléfono (API REST §3.1). El número es texto para
 * conservar los ceros iniciales; el dueño se indica con su cédula.
 */
public record TelefonoRequest(String number, String company, Integer ownerId) {
}
