package co.edu.javeriana.as.personapp.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PersonPhonesInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PersonPhonesUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.mapper.PersonaTelefonosMapperRest;
import co.edu.javeriana.as.personapp.model.response.PersonaTelefonosResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST de la consulta de una persona con sus teléfonos (HU-033). Crea
 * el caso de uso en cada llamada con los puertos de Persona y Teléfono del motor
 * pedido, así que la persona y sus teléfonos salen del mismo motor (C-03, C-09).
 */
@Slf4j
@Adapter
public class PersonaTelefonosInputAdapterRest {

	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;

	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;

	@Autowired
	@Qualifier("phoneOutputAdapterMaria")
	private PhoneOutputPort phoneOutputPortMaria;

	@Autowired
	@Qualifier("phoneOutputAdapterMongo")
	private PhoneOutputPort phoneOutputPortMongo;

	@Autowired
	private PersonaTelefonosMapperRest personaTelefonosMapperRest;

	/** ERR-02 si el motor no es MARIA ni MONGO; no se usa uno por defecto. */
	private PersonPhonesInputPort useCase(String database) throws InvalidOptionException {
		if (DatabaseOption.MARIA.name().equalsIgnoreCase(database))
			return new PersonPhonesUseCase(personOutputPortMaria, phoneOutputPortMaria);
		if (DatabaseOption.MONGO.name().equalsIgnoreCase(database))
			return new PersonPhonesUseCase(personOutputPortMongo, phoneOutputPortMongo);
		throw new InvalidOptionException("El motor " + database + " no es válido; use MARIA o MONGO");
	}

	public PersonaTelefonosResponse consultarConTelefonos(String database, Integer cc)
			throws InvalidOptionException, InvalidDataException, NoExistException {
		log.debug("Into consultarConTelefonos Persona in Input Adapter");
		return personaTelefonosMapperRest.fromDomainToAdapterRest(useCase(database).findWithPhones(cc));
	}
}
