package co.edu.javeriana.as.personapp.terminal.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PersonInputPort;
import co.edu.javeriana.as.personapp.application.port.in.PersonPhonesInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PersonPhonesUseCase;
import co.edu.javeriana.as.personapp.application.usecase.PersonUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.mapper.PersonaMapperCli;
import co.edu.javeriana.as.personapp.terminal.mapper.TelefonoMapperCli;
import co.edu.javeriana.as.personapp.terminal.model.PersonaModelCli;
import co.edu.javeriana.as.personapp.terminal.model.PersonaTelefonosModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador CLI de Persona. Crea el caso de uso en cada operación con el puerto
 * del motor elegido, sin guardarlo en un campo compartido, para que el cambio de
 * motor del menú no deje el anterior colgado (C-09, ADR-002). Sigue el patrón que
 * HU-018 introdujo en Profesión.
 */
@Slf4j
@Adapter
public class PersonaInputAdapterCli {

	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;

	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;

	@Autowired
	private PersonaMapperCli personaMapperCli;

	@Autowired
	@Qualifier("phoneOutputAdapterMaria")
	private PhoneOutputPort phoneOutputPortMaria;

	@Autowired
	@Qualifier("phoneOutputAdapterMongo")
	private PhoneOutputPort phoneOutputPortMongo;

	@Autowired
	private TelefonoMapperCli telefonoMapperCli;

	/** ERR-02 si el motor no es MARIA ni MONGO; no se usa uno por defecto. */
	private PersonInputPort useCase(DatabaseOption database) throws InvalidOptionException {
		if (DatabaseOption.MARIA == database) {
			return new PersonUseCase(personOutputPortMaria);
		}
		if (DatabaseOption.MONGO == database) {
			return new PersonUseCase(personOutputPortMongo);
		}
		throw new InvalidOptionException("El motor " + database + " no es válido; use MARIA o MONGO");
	}

	public PersonaModelCli consultar(DatabaseOption database, Integer cc)
			throws InvalidOptionException, NoExistException {
		log.debug("Into consultar Persona in Input Adapter CLI");
		return personaMapperCli.fromDomainToAdapterCli(useCase(database).findOne(cc));
	}

	public List<PersonaModelCli> listar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into listar Persona in Input Adapter CLI");
		return useCase(database).findAll().stream().map(personaMapperCli::fromDomainToAdapterCli).toList();
	}

	public Integer contar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into contar Persona in Input Adapter CLI");
		return useCase(database).count();
	}

	public List<PersonaModelCli> listarPagina(DatabaseOption database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Persona in Input Adapter CLI");
		return useCase(database).findAll(page, size).stream().map(personaMapperCli::fromDomainToAdapterCli).toList();
	}

	public PersonaModelCli crear(DatabaseOption database, PersonaModelCli persona)
			throws InvalidOptionException, InvalidDataException, DuplicateException {
		log.debug("Into crear Persona in Input Adapter CLI");
		return personaMapperCli.fromDomainToAdapterCli(
				useCase(database).create(personaMapperCli.fromAdapterCliToDomain(persona)));
	}

	public PersonaModelCli editar(DatabaseOption database, Integer cc, PersonaModelCli persona)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException {
		log.debug("Into editar Persona in Input Adapter CLI");
		return personaMapperCli.fromDomainToAdapterCli(
				useCase(database).edit(cc, personaMapperCli.fromAdapterCliToDomain(persona)));
	}

	public void eliminar(DatabaseOption database, Integer cc)
			throws InvalidOptionException, NoExistException, DependentRecordsException {
		log.debug("Into eliminar Persona in Input Adapter CLI");
		useCase(database).drop(cc);
	}

	/**
	 * HU-033: la persona con todos sus teléfonos, leídos del mismo motor (C-03). Es una
	 * consulta adicional; {@link #consultar} sigue sin colecciones (C-10).
	 */
	public PersonaTelefonosModelCli consultarConTelefonos(DatabaseOption database, Integer cc)
			throws InvalidOptionException, InvalidDataException, NoExistException {
		log.debug("Into consultarConTelefonos Persona in Input Adapter CLI");
		PersonPhonesInputPort port;
		if (DatabaseOption.MARIA == database)
			port = new PersonPhonesUseCase(personOutputPortMaria, phoneOutputPortMaria);
		else if (DatabaseOption.MONGO == database)
			port = new PersonPhonesUseCase(personOutputPortMongo, phoneOutputPortMongo);
		else
			throw new InvalidOptionException("El motor " + database + " no es válido; use MARIA o MONGO");
		var person = port.findWithPhones(cc);
		return new PersonaTelefonosModelCli(personaMapperCli.fromDomainToAdapterCli(person),
				person.getPhoneNumbers().stream().map(telefonoMapperCli::fromDomainToAdapterCli).toList());
	}
}
