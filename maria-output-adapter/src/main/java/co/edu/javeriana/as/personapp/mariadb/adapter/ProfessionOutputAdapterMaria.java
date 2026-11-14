package co.edu.javeriana.as.personapp.mariadb.adapter;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mariadb.entity.ProfesionEntity;
import co.edu.javeriana.as.personapp.mariadb.mapper.ProfesionMapperMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.ProfesionRepositoryMaria;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistencia de Profesión en MariaDB. Devuelve las profesiones sin sus
 * estudios (C-10): la colección no se carga.
 */
@Slf4j
@Adapter("professionOutputAdapterMaria")
@Transactional
public class ProfessionOutputAdapterMaria implements ProfessionOutputPort {

	/** Orden de C-06: id ascendente. */
	private static final Sort BY_ID = Sort.by(Sort.Direction.ASC, "id");

	@Autowired
	private ProfesionRepositoryMaria profesionRepositoryMaria;

	@Autowired
	private ProfesionMapperMaria profesionMapperMaria;

	@Override
	public Profession save(Profession profession) {
		log.debug("Into save on Adapter MariaDB");
		ProfesionEntity persisted = profesionRepositoryMaria.save(profesionMapperMaria.fromDomainToAdapter(profession));
		return profesionMapperMaria.fromAdapterToDomainBasic(persisted);
	}

	@Override
	public Boolean delete(Integer identification) {
		log.debug("Into delete on Adapter MariaDB");
		profesionRepositoryMaria.deleteById(identification);
		return !profesionRepositoryMaria.existsById(identification);
	}

	@Override
	public List<Profession> find() {
		log.debug("Into find on Adapter MariaDB");
		return profesionRepositoryMaria.findAll(BY_ID).stream().map(profesionMapperMaria::fromAdapterToDomainBasic)
				.toList();
	}

	@Override
	public List<Profession> find(Integer page, Integer size) {
		log.debug("Into find page on Adapter MariaDB");
		return profesionRepositoryMaria.findAll(PageRequest.of(page, size, BY_ID)).stream()
				.map(profesionMapperMaria::fromAdapterToDomainBasic).toList();
	}

	@Override
	public Profession findById(Integer identification) {
		log.debug("Into findById on Adapter MariaDB");
		return profesionRepositoryMaria.findById(identification).map(profesionMapperMaria::fromAdapterToDomainBasic)
				.orElse(null);
	}

	@Override
	public Integer count() {
		log.debug("Into count on Adapter MariaDB");
		return Math.toIntExact(profesionRepositoryMaria.count());
	}

	@Override
	public Boolean hasStudies(Integer identification) {
		log.debug("Into hasStudies on Adapter MariaDB");
		return profesionRepositoryMaria.hasStudies(identification);
	}
}
