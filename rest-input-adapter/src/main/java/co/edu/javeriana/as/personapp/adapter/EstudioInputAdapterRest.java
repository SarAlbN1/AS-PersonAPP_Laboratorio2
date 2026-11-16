package co.edu.javeriana.as.personapp.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.StudyInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.StudyUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.mapper.EstudioMapperRest;
import co.edu.javeriana.as.personapp.model.request.EstudioRequest;
import co.edu.javeriana.as.personapp.model.response.EstudioResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador REST de Estudios. Crea el caso de uso en cada operación con los puertos
 * de Estudios, Persona y Profesión del motor pedido, para que las dos referencias se
 * comprueben en ese mismo motor (C-03) y dos solicitudes concurrentes no se cambien
 * el motor (C-09).
 */
@Slf4j
@Adapter
public class EstudioInputAdapterRest {

	@Autowired
	@Qualifier("studyOutputAdapterMaria")
	private StudyOutputPort studyOutputPortMaria;

	@Autowired
	@Qualifier("studyOutputAdapterMongo")
	private StudyOutputPort studyOutputPortMongo;

	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;

	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;

	@Autowired
	@Qualifier("professionOutputAdapterMaria")
	private ProfessionOutputPort professionOutputPortMaria;

	@Autowired
	@Qualifier("professionOutputAdapterMongo")
	private ProfessionOutputPort professionOutputPortMongo;

	@Autowired
	private EstudioMapperRest estudioMapperRest;

	/** ERR-02 si el motor no es MARIA ni MONGO; no se usa uno por defecto. */
	private StudyInputPort useCase(String database) throws InvalidOptionException {
		if (DatabaseOption.MARIA.name().equalsIgnoreCase(database))
			return new StudyUseCase(studyOutputPortMaria, personOutputPortMaria, professionOutputPortMaria);
		if (DatabaseOption.MONGO.name().equalsIgnoreCase(database))
			return new StudyUseCase(studyOutputPortMongo, personOutputPortMongo, professionOutputPortMongo);
		throw new InvalidOptionException("El motor " + database + " no es válido; use MARIA o MONGO");
	}

	public EstudioResponse consultar(String database, Integer professionId, Integer personId)
			throws InvalidOptionException, NoExistException {
		log.debug("Into consultar Estudio in Input Adapter");
		return estudioMapperRest.fromDomainToAdapterRest(useCase(database).findOne(professionId, personId));
	}

	public List<EstudioResponse> listar(String database) throws InvalidOptionException {
		log.debug("Into listar Estudio in Input Adapter");
		return useCase(database).findAll().stream().map(estudioMapperRest::fromDomainToAdapterRest).toList();
	}

	public Integer contar(String database) throws InvalidOptionException {
		log.debug("Into contar Estudio in Input Adapter");
		return useCase(database).count();
	}

	public EstudioResponse crear(String database, EstudioRequest request)
			throws InvalidOptionException, InvalidDataException, DuplicateException, ReferenceNotFoundException {
		log.debug("Into crear Estudio in Input Adapter");
		StudyInputPort port = useCase(database);
		if (request == null)
			throw new InvalidDataException("El cuerpo del estudio es obligatorio");
		return estudioMapperRest.fromDomainToAdapterRest(port.create(estudioMapperRest.fromAdapterToDomain(request)));
	}

	public EstudioResponse editar(String database, Integer professionId, Integer personId, EstudioRequest request)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException {
		log.debug("Into editar Estudio in Input Adapter");
		StudyInputPort port = useCase(database);
		if (request == null)
			throw new InvalidDataException("El cuerpo del estudio es obligatorio");
		return estudioMapperRest.fromDomainToAdapterRest(
				port.edit(professionId, personId, estudioMapperRest.fromAdapterToDomain(request)));
	}

	public void eliminar(String database, Integer professionId, Integer personId)
			throws InvalidOptionException, NoExistException {
		log.debug("Into eliminar Estudio in Input Adapter");
		useCase(database).drop(professionId, personId);
	}

	public List<EstudioResponse> listarPagina(String database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Estudio in Input Adapter");
		return useCase(database).findAll(page, size).stream().map(estudioMapperRest::fromDomainToAdapterRest)
				.toList();
	}
}
