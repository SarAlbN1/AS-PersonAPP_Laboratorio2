package co.edu.javeriana.as.personapp.terminal.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PhoneInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PhoneUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.terminal.mapper.TelefonoMapperCli;
import co.edu.javeriana.as.personapp.terminal.model.TelefonoModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador CLI de Teléfono. El menú indica el motor en cada operación y el caso
 * de uso se crea para ella con los puertos de Teléfono y de Persona de ese motor,
 * sin estado compartido (C-03, C-09).
 */
@Slf4j
@Adapter
public class TelefonoInputAdapterCli {

	@Autowired
	@Qualifier("phoneOutputAdapterMaria")
	private PhoneOutputPort phoneOutputPortMaria;

	@Autowired
	@Qualifier("phoneOutputAdapterMongo")
	private PhoneOutputPort phoneOutputPortMongo;

	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;

	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;

	@Autowired
	private TelefonoMapperCli telefonoMapperCli;

	private PhoneInputPort useCase(DatabaseOption database) throws InvalidOptionException {
		if (database == DatabaseOption.MARIA)
			return new PhoneUseCase(phoneOutputPortMaria, personOutputPortMaria);
		if (database == DatabaseOption.MONGO)
			return new PhoneUseCase(phoneOutputPortMongo, personOutputPortMongo);
		throw new InvalidOptionException("Motor no válido; use MariaDB o MongoDB");
	}

	public TelefonoModelCli consultar(DatabaseOption database, String numero)
			throws InvalidOptionException, InvalidDataException, NoExistException {
		log.debug("Into consultar Telefono in CLI Adapter");
		return telefonoMapperCli.fromDomainToAdapterCli(useCase(database).findOne(numero));
	}

	public List<TelefonoModelCli> listar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into listar Telefono in CLI Adapter");
		return useCase(database).findAll().stream().map(telefonoMapperCli::fromDomainToAdapterCli).toList();
	}

	public Integer contar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into contar Telefono in CLI Adapter");
		return useCase(database).count();
	}

	public TelefonoModelCli crear(DatabaseOption database, TelefonoModelCli telefono)
			throws InvalidOptionException, InvalidDataException, DuplicateException, ReferenceNotFoundException {
		log.debug("Into crear Telefono in CLI Adapter");
		return telefonoMapperCli.fromDomainToAdapterCli(
				useCase(database).create(telefonoMapperCli.fromAdapterCliToDomain(telefono)));
	}

	public TelefonoModelCli editar(DatabaseOption database, String numero, TelefonoModelCli telefono)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException,
			ReferenceNotFoundException {
		log.debug("Into editar Telefono in CLI Adapter");
		return telefonoMapperCli.fromDomainToAdapterCli(
				useCase(database).edit(numero, telefonoMapperCli.fromAdapterCliToDomain(telefono)));
	}

	public void eliminar(DatabaseOption database, String numero)
			throws InvalidOptionException, InvalidDataException, NoExistException {
		log.debug("Into eliminar Telefono in CLI Adapter");
		useCase(database).drop(numero);
	}

	public List<TelefonoModelCli> listarPagina(DatabaseOption database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Telefono in CLI Adapter");
		return useCase(database).findAll(page, size).stream().map(telefonoMapperCli::fromDomainToAdapterCli)
				.toList();
	}
}
