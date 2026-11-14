package co.edu.javeriana.as.personapp.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.ProfessionInputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.ProfessionUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.mapper.ProfesionMapperRest;
import co.edu.javeriana.as.personapp.model.request.ProfesionRequest;
import co.edu.javeriana.as.personapp.model.response.ProfesionResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST de Profesión. Crea el caso de uso en cada operación con el
 * puerto del motor pedido, sin guardarlo en un campo compartido, para que dos
 * solicitudes concurrentes no se cambien el motor (C-09, ADR-002).
 */
@Slf4j
@Adapter
public class ProfesionInputAdapterRest {

	@Autowired
	@Qualifier("professionOutputAdapterMaria")
	private ProfessionOutputPort professionOutputPortMaria;

	@Autowired
	@Qualifier("professionOutputAdapterMongo")
	private ProfessionOutputPort professionOutputPortMongo;

	@Autowired
	private ProfesionMapperRest profesionMapperRest;

	/** ERR-02 si el motor no es MARIA ni MONGO; no se usa uno por defecto. */
	private ProfessionInputPort useCase(String database) throws InvalidOptionException {
		if (DatabaseOption.MARIA.name().equalsIgnoreCase(database)) {
			return new ProfessionUseCase(professionOutputPortMaria);
		}
		if (DatabaseOption.MONGO.name().equalsIgnoreCase(database)) {
			return new ProfessionUseCase(professionOutputPortMongo);
		}
		throw new InvalidOptionException("El motor " + database + " no es válido; use MARIA o MONGO");
	}

	public ProfesionResponse consultar(String database, Integer id) throws InvalidOptionException, NoExistException {
		log.debug("Into consultar Profesion in Input Adapter");
		return profesionMapperRest.fromDomainToAdapterRest(useCase(database).findOne(id));
	}

	public List<ProfesionResponse> listar(String database) throws InvalidOptionException {
		log.debug("Into listar Profesion in Input Adapter");
		return useCase(database).findAll().stream().map(profesionMapperRest::fromDomainToAdapterRest).toList();
	}

	public Integer contar(String database) throws InvalidOptionException {
		log.debug("Into contar Profesion in Input Adapter");
		return useCase(database).count();
	}

	public ProfesionResponse crear(String database, ProfesionRequest request)
			throws InvalidOptionException, InvalidDataException, DuplicateException {
		log.debug("Into crear Profesion in Input Adapter");
		ProfessionInputPort port = useCase(database);
		if (request == null)
			throw new InvalidDataException("El cuerpo de la profesión es obligatorio");
		return profesionMapperRest.fromDomainToAdapterRest(port.create(profesionMapperRest.fromAdapterToDomain(request)));
	}

	public ProfesionResponse editar(String database, Integer id, ProfesionRequest request)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException {
		log.debug("Into editar Profesion in Input Adapter");
		ProfessionInputPort port = useCase(database);
		if (request == null)
			throw new InvalidDataException("El cuerpo de la profesión es obligatorio");
		return profesionMapperRest
				.fromDomainToAdapterRest(port.edit(id, profesionMapperRest.fromAdapterToDomain(request)));
	}

	public void eliminar(String database, Integer id)
			throws InvalidOptionException, NoExistException, DependentRecordsException {
		log.debug("Into eliminar Profesion in Input Adapter");
		useCase(database).drop(id);
	}

	public List<ProfesionResponse> listarPagina(String database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Profesion in Input Adapter");
		return useCase(database).findAll(page, size).stream().map(profesionMapperRest::fromDomainToAdapterRest)
				.toList();
	}
}
