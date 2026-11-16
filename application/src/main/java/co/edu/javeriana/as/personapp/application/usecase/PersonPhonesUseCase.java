package co.edu.javeriana.as.personapp.application.usecase;

import co.edu.javeriana.as.personapp.application.port.in.PersonPhonesInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Person;
import lombok.extern.slf4j.Slf4j;

/**
 * HU-033: arma la persona con sus teléfonos a partir de los puertos existentes de
 * Persona y Teléfono del mismo motor. El adaptador de entrada lo crea en cada
 * operación con los puertos del motor elegido, sin estado compartido (ADR-002).
 */
@Slf4j
public class PersonPhonesUseCase implements PersonPhonesInputPort {

	private final PersonOutputPort personPersistence;
	private final PhoneOutputPort phonePersistence;

	public PersonPhonesUseCase(PersonOutputPort personPersistence, PhoneOutputPort phonePersistence) {
		this.personPersistence = personPersistence;
		this.phonePersistence = phonePersistence;
	}

	@Override
	public Person findWithPhones(Integer identification) throws InvalidDataException, NoExistException {
		if (identification == null || identification < 1)
			throw new InvalidDataException("La cédula debe ser un entero mayor o igual a 1");
		Person person = personPersistence.findById(identification);
		if (person == null)
			throw new NoExistException("No existe una persona con cc " + identification);
		log.debug("Into findWithPhones on Application Domain");
		person.setPhoneNumbers(phonePersistence.findByOwner(identification));
		// La consulta es de teléfonos: los estudios no se exponen aquí (C-10).
		person.setStudies(null);
		return person;
	}
}
