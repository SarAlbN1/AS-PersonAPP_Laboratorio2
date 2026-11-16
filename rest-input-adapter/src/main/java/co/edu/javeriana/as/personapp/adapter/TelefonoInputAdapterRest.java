package co.edu.javeriana.as.personapp.adapter;

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
import co.edu.javeriana.as.personapp.mapper.TelefonoMapperRest;
import co.edu.javeriana.as.personapp.model.request.TelefonoRequest;
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST de Teléfono. Crea el caso de uso en cada operación con los puertos
 * de Teléfono y de Persona del motor pedido, para que el dueño se compruebe en ese
 * mismo motor (C-03) y dos solicitudes concurrentes no se cambien el motor (C-09).
 */
@Slf4j
@Adapter
public class TelefonoInputAdapterRest {

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
	private TelefonoMapperRest telefonoMapperRest;

	/** ERR-02 si el motor no es MARIA ni MONGO; no se usa uno por defecto. */
	private PhoneInputPort useCase(String database) throws InvalidOptionException {
		if (DatabaseOption.MARIA.name().equalsIgnoreCase(database))
			return new PhoneUseCase(phoneOutputPortMaria, personOutputPortMaria);
		if (DatabaseOption.MONGO.name().equalsIgnoreCase(database))
			return new PhoneUseCase(phoneOutputPortMongo, personOutputPortMongo);
		throw new InvalidOptionException("El motor " + database + " no es válido; use MARIA o MONGO");
	}

	public TelefonoResponse consultar(String database, String number)
			throws InvalidOptionException, InvalidDataException, NoExistException {
		log.debug("Into consultar Telefono in Input Adapter");
		return telefonoMapperRest.fromDomainToAdapterRest(useCase(database).findOne(number));
	}

	public List<TelefonoResponse> listar(String database) throws InvalidOptionException {
		log.debug("Into listar Telefono in Input Adapter");
		return useCase(database).findAll().stream().map(telefonoMapperRest::fromDomainToAdapterRest).toList();
	}

	public Integer contar(String database) throws InvalidOptionException {
		log.debug("Into contar Telefono in Input Adapter");
		return useCase(database).count();
	}

	public TelefonoResponse crear(String database, TelefonoRequest request)
			throws InvalidOptionException, InvalidDataException, DuplicateException, ReferenceNotFoundException {
		log.debug("Into crear Telefono in Input Adapter");
		PhoneInputPort port = useCase(database);
		if (request == null)
			throw new InvalidDataException("El cuerpo del teléfono es obligatorio");
		return telefonoMapperRest.fromDomainToAdapterRest(port.create(telefonoMapperRest.fromAdapterToDomain(request)));
	}

	public TelefonoResponse editar(String database, String number, TelefonoRequest request)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException,
			ReferenceNotFoundException {
		log.debug("Into editar Telefono in Input Adapter");
		PhoneInputPort port = useCase(database);
		if (request == null)
			throw new InvalidDataException("El cuerpo del teléfono es obligatorio");
		return telefonoMapperRest
				.fromDomainToAdapterRest(port.edit(number, telefonoMapperRest.fromAdapterToDomain(request)));
	}

	public void eliminar(String database, String number)
			throws InvalidOptionException, InvalidDataException, NoExistException {
		log.debug("Into eliminar Telefono in Input Adapter");
		useCase(database).drop(number);
	}

	public List<TelefonoResponse> listarPagina(String database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Telefono in Input Adapter");
		return useCase(database).findAll(page, size).stream().map(telefonoMapperRest::fromDomainToAdapterRest)
				.toList();
	}
}
