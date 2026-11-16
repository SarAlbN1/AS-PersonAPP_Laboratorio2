package co.edu.javeriana.as.personapp.terminal.adapter;

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
import co.edu.javeriana.as.personapp.terminal.mapper.EstudioMapperCli;
import co.edu.javeriana.as.personapp.terminal.model.EstudioModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador CLI de Estudios. El menú indica el motor en cada operación y el caso de
 * uso se crea para ella con los puertos de Estudios, Persona y Profesión de ese motor,
 * sin estado compartido (C-03, C-09).
 */
@Slf4j
@Adapter
public class EstudioInputAdapterCli {

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
	private EstudioMapperCli estudioMapperCli;

	private StudyInputPort useCase(DatabaseOption database) throws InvalidOptionException {
		if (database == DatabaseOption.MARIA)
			return new StudyUseCase(studyOutputPortMaria, personOutputPortMaria, professionOutputPortMaria);
		if (database == DatabaseOption.MONGO)
			return new StudyUseCase(studyOutputPortMongo, personOutputPortMongo, professionOutputPortMongo);
		throw new InvalidOptionException("Motor no válido; use MariaDB o MongoDB");
	}

	public EstudioModelCli consultar(DatabaseOption database, Integer idProfesion, Integer ccPersona)
			throws InvalidOptionException, NoExistException {
		log.debug("Into consultar Estudio in CLI Adapter");
		return estudioMapperCli.fromDomainToAdapterCli(useCase(database).findOne(idProfesion, ccPersona));
	}

	public List<EstudioModelCli> listar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into listar Estudio in CLI Adapter");
		return useCase(database).findAll().stream().map(estudioMapperCli::fromDomainToAdapterCli).toList();
	}

	public Integer contar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into contar Estudio in CLI Adapter");
		return useCase(database).count();
	}

	public EstudioModelCli crear(DatabaseOption database, EstudioModelCli estudio)
			throws InvalidOptionException, InvalidDataException, DuplicateException, ReferenceNotFoundException {
		log.debug("Into crear Estudio in CLI Adapter");
		return estudioMapperCli.fromDomainToAdapterCli(
				useCase(database).create(estudioMapperCli.fromAdapterCliToDomain(estudio)));
	}

	public EstudioModelCli editar(DatabaseOption database, Integer idProfesion, Integer ccPersona,
			EstudioModelCli estudio)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException {
		log.debug("Into editar Estudio in CLI Adapter");
		return estudioMapperCli.fromDomainToAdapterCli(
				useCase(database).edit(idProfesion, ccPersona, estudioMapperCli.fromAdapterCliToDomain(estudio)));
	}

	public void eliminar(DatabaseOption database, Integer idProfesion, Integer ccPersona)
			throws InvalidOptionException, NoExistException {
		log.debug("Into eliminar Estudio in CLI Adapter");
		useCase(database).drop(idProfesion, ccPersona);
	}

	public List<EstudioModelCli> listarPagina(DatabaseOption database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Estudio in CLI Adapter");
		return useCase(database).findAll(page, size).stream().map(estudioMapperCli::fromDomainToAdapterCli)
				.toList();
	}
}
