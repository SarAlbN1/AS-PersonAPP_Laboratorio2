package co.edu.javeriana.as.personapp.application.port.in;

import co.edu.javeriana.as.personapp.common.annotations.Port;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Person;

/**
 * HU-033: consulta de una persona con todos sus teléfonos. Es una consulta adicional;
 * no reemplaza la consulta por identificador de Persona, que no trae colecciones (C-10).
 */
@Port
public interface PersonPhonesInputPort {

	/**
	 * La persona con esa cédula y sus teléfonos ordenados por número (C-06). Si no
	 * tiene teléfonos, la lista queda vacía; sus estudios no se cargan.
	 */
	public Person findWithPhones(Integer identification) throws InvalidDataException, NoExistException;
}
