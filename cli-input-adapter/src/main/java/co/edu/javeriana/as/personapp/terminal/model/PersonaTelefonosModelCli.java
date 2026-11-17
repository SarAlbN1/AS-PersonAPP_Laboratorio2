package co.edu.javeriana.as.personapp.terminal.model;

import java.util.List;

/** Persona con todos sus teléfonos, ordenados por número como texto (HU-033). */
public record PersonaTelefonosModelCli(PersonaModelCli persona, List<TelefonoModelCli> telefonos) {
}
