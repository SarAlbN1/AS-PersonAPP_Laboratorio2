package co.edu.javeriana.as.personapp.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PersonInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PersonUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.mapper.PersonaMapperRest;
import co.edu.javeriana.as.personapp.model.request.PersonaRequest;
import co.edu.javeriana.as.personapp.model.response.PersonaResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST de Persona. Crea el caso de uso en cada operación con el puerto
 * del motor pedido, sin guardarlo en un campo compartido, para que dos solicitudes
 * concurrentes no se cambien el motor entre ellas (C-09, ADR-002). Sigue el patrón
 * que HU-016 introdujo en Profesión.
 */
@Slf4j
@Adapter
public class PersonaInputAdapterRest {

	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;

	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;

	@Autowired
	private PersonaMapperRest personaMapperRest;

	/** ERR-02 si el motor no es MARIA ni MONGO; no se usa uno por defecto. */
	private PersonInputPort useCase(String database) throws InvalidOptionException {
		if (DatabaseOption.MARIA.name().equalsIgnoreCase(database)) {
			return new PersonUseCase(personOutputPortMaria);
		}
		if (DatabaseOption.MONGO.name().equalsIgnoreCase(database)) {
			return new PersonUseCase(personOutputPortMongo);
		}
		throw new InvalidOptionException("El motor " + database + " no es válido; use MARIA o MONGO");
	}

	/** Motor normalizado, para rotular la respuesta con el que de verdad se usó. */
	private String motor(String database) {
		return database.toUpperCase();
	}

	public PersonaResponse consultar(String database, Integer cc) throws InvalidOptionException, NoExistException {
		log.debug("Into consultar Persona in Input Adapter");
		return personaMapperRest.fromDomainToAdapterRest(useCase(database).findOne(cc), motor(database));
	}

	public List<PersonaResponse> listar(String database) throws InvalidOptionException {
		log.debug("Into listar Persona in Input Adapter");
		return useCase(database).findAll().stream()
				.map(person -> personaMapperRest.fromDomainToAdapterRest(person, motor(database))).toList();
	}

	public Integer contar(String database) throws InvalidOptionException {
		log.debug("Into contar Persona in Input Adapter");
		return useCase(database).count();
	}

	public List<PersonaResponse> listarPagina(String database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Persona in Input Adapter");
		return useCase(database).findAll(page, size).stream()
				.map(person -> personaMapperRest.fromDomainToAdapterRest(person, motor(database))).toList();
	}

	public PersonaResponse crear(String database, PersonaRequest request)
			throws InvalidOptionException, InvalidDataException, DuplicateException {
		log.debug("Into crear Persona in Input Adapter");
		PersonInputPort useCase = useCase(database);
		return personaMapperRest.fromDomainToAdapterRest(
				useCase.create(personaMapperRest.fromAdapterToDomain(request)), motor(database));
	}

	public PersonaResponse editar(String database, Integer cc, PersonaRequest request)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException {
		log.debug("Into editar Persona in Input Adapter");
		PersonInputPort useCase = useCase(database);
		return personaMapperRest.fromDomainToAdapterRest(
				useCase.edit(cc, personaMapperRest.fromAdapterToDomain(request)), motor(database));
	}

	public void eliminar(String database, Integer cc)
			throws InvalidOptionException, NoExistException, DependentRecordsException {
		log.debug("Into eliminar Persona in Input Adapter");
		useCase(database).drop(cc);
	}
}
