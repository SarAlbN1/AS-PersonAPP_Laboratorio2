package co.edu.javeriana.as.personapp.terminal.adapter;

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
import co.edu.javeriana.as.personapp.terminal.mapper.ProfesionMapperCli;
import co.edu.javeriana.as.personapp.terminal.model.ProfesionModelCli;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador CLI de Profesión. El menú indica el motor en cada operación y el
 * caso de uso se crea para esa operación, sin estado compartido (C-09).
 */
@Slf4j
@Adapter
public class ProfesionInputAdapterCli {

	@Autowired
	@Qualifier("professionOutputAdapterMaria")
	private ProfessionOutputPort professionOutputPortMaria;

	@Autowired
	@Qualifier("professionOutputAdapterMongo")
	private ProfessionOutputPort professionOutputPortMongo;

	@Autowired
	private ProfesionMapperCli profesionMapperCli;

	private ProfessionInputPort useCase(DatabaseOption database) throws InvalidOptionException {
		if (database == DatabaseOption.MARIA)
			return new ProfessionUseCase(professionOutputPortMaria);
		if (database == DatabaseOption.MONGO)
			return new ProfessionUseCase(professionOutputPortMongo);
		throw new InvalidOptionException("Motor no válido; use MariaDB o MongoDB");
	}

	public ProfesionModelCli consultar(DatabaseOption database, Integer id)
			throws InvalidOptionException, NoExistException {
		log.debug("Into consultar Profesion in CLI Adapter");
		return profesionMapperCli.fromDomainToAdapterCli(useCase(database).findOne(id));
	}

	public List<ProfesionModelCli> listar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into listar Profesion in CLI Adapter");
		return useCase(database).findAll().stream().map(profesionMapperCli::fromDomainToAdapterCli).toList();
	}

	public Integer contar(DatabaseOption database) throws InvalidOptionException {
		log.debug("Into contar Profesion in CLI Adapter");
		return useCase(database).count();
	}

	public ProfesionModelCli crear(DatabaseOption database, ProfesionModelCli profesion)
			throws InvalidOptionException, InvalidDataException, DuplicateException {
		log.debug("Into crear Profesion in CLI Adapter");
		return profesionMapperCli.fromDomainToAdapterCli(
				useCase(database).create(profesionMapperCli.fromAdapterCliToDomain(profesion)));
	}

	public ProfesionModelCli editar(DatabaseOption database, Integer id, ProfesionModelCli profesion)
			throws InvalidOptionException, InvalidDataException, KeyMismatchException, NoExistException {
		log.debug("Into editar Profesion in CLI Adapter");
		return profesionMapperCli.fromDomainToAdapterCli(
				useCase(database).edit(id, profesionMapperCli.fromAdapterCliToDomain(profesion)));
	}

	public void eliminar(DatabaseOption database, Integer id)
			throws InvalidOptionException, NoExistException, DependentRecordsException {
		log.debug("Into eliminar Profesion in CLI Adapter");
		useCase(database).drop(id);
	}

	public List<ProfesionModelCli> listarPagina(DatabaseOption database, Integer page, Integer size)
			throws InvalidOptionException, InvalidDataException {
		log.debug("Into listarPagina Profesion in CLI Adapter");
		return useCase(database).findAll(page, size).stream().map(profesionMapperCli::fromDomainToAdapterCli)
				.toList();
	}
}
