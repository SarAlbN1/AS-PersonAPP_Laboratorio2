package co.edu.javeriana.as.personapp.mariadb.adapter;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntityPK;
import co.edu.javeriana.as.personapp.mariadb.mapper.EstudiosMapperMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.EstudiosRepositoryMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.PersonaRepositoryMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.ProfesionRepositoryMaria;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistencia de Estudios en MariaDB con la clave compuesta (id_prof, cc_per).
 * Devuelve cada estudio con su persona y su profesión básicas, sin las colecciones
 * de estas (C-10).
 */
@Slf4j
@Adapter("studyOutputAdapterMaria")
@Transactional
public class StudyOutputAdapterMaria implements StudyOutputPort {

	/** Orden de C-06: id de profesión y luego cc de persona, pedido al motor. */
	private static final Sort BY_KEY = Sort.by(Sort.Direction.ASC, "estudiosEntityPK.idProf", "estudiosEntityPK.ccPer");

	@Autowired
	private EstudiosRepositoryMaria estudiosRepositoryMaria;

	@Autowired
	private PersonaRepositoryMaria personaRepositoryMaria;

	@Autowired
	private ProfesionRepositoryMaria profesionRepositoryMaria;

	@Autowired
	private EstudiosMapperMaria estudiosMapperMaria;

	@Override
	public Study save(Study study) {
		log.debug("Into save on Adapter MariaDB");
		EstudiosEntity entity = estudiosMapperMaria.fromDomainToAdapter(study);
		// Las columnas de la clave se escriben desde el PK; las relaciones son de solo
		// lectura (insertable=false) y se enlazan a las filas existentes para que el
		// resultado traiga la persona y la profesión. El caso de uso ya comprobó que existen (C-03).
		entity.setPersona(personaRepositoryMaria.getReferenceById(study.getPerson().getIdentification()));
		entity.setProfesion(profesionRepositoryMaria.getReferenceById(study.getProfession().getIdentification()));
		return estudiosMapperMaria.fromAdapterToDomain(estudiosRepositoryMaria.save(entity));
	}

	@Override
	public Boolean delete(Integer professionId, Integer personId) {
		log.debug("Into delete on Adapter MariaDB");
		EstudiosEntityPK key = new EstudiosEntityPK(professionId, personId);
		estudiosRepositoryMaria.deleteById(key);
		return !estudiosRepositoryMaria.existsById(key);
	}

	@Override
	public List<Study> find() {
		log.debug("Into find on Adapter MariaDB");
		return estudiosRepositoryMaria.findAll(BY_KEY).stream().map(estudiosMapperMaria::fromAdapterToDomain).toList();
	}

	@Override
	public List<Study> find(Integer page, Integer size) {
		log.debug("Into find page on Adapter MariaDB");
		return estudiosRepositoryMaria.findAll(PageRequest.of(page, size, BY_KEY)).stream()
				.map(estudiosMapperMaria::fromAdapterToDomain).toList();
	}

	@Override
	public Study findById(Integer professionId, Integer personId) {
		log.debug("Into findById on Adapter MariaDB");
		return estudiosRepositoryMaria.findById(new EstudiosEntityPK(professionId, personId))
				.map(estudiosMapperMaria::fromAdapterToDomain).orElse(null);
	}

	@Override
	public Integer count() {
		log.debug("Into count on Adapter MariaDB");
		return Math.toIntExact(estudiosRepositoryMaria.count());
	}
}
