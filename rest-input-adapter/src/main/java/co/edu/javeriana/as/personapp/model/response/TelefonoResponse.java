package co.edu.javeriana.as.personapp.model.response;

/** Teléfono devuelto por REST, con la cédula de su dueño (C-10). */
public record TelefonoResponse(String number, String company, Integer ownerId) {
}
