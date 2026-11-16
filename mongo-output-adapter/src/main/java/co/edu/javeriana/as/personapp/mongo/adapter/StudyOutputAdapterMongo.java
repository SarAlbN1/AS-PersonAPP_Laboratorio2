package co.edu.javeriana.as.personapp.mongo.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mongo.mapper.EstudiosMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.EstudiosRepositoryMongo;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistencia de Estudios en MongoDB. La clave del puerto es (profesión, persona),
 * en el orden de la ruta REST; el _id guardado es "ccPersona-idProfesion" (C-10).
 */
@Slf4j
@Adapter("studyOutputAdapterMongo")
public class StudyOutputAdapterMongo implements StudyOutputPort {

	/**
	 * Orden de C-06 pedido al motor: id de profesión y luego cc de persona. No se
	 * ordena por _id porque es texto con la persona primero. Las referencias guardan
	 * el id numérico, así que el orden es numérico.
	 */
	private static final Sort BY_KEY = Sort.by(Sort.Direction.ASC, "primaryProfesion", "primaryPersona");

	@Autowired
	private EstudiosRepositoryMongo estudiosRepositoryMongo;

	@Autowired
	private EstudiosMapperMongo estudiosMapperMongo;

	@Override
	public Study save(Study study) {
		log.debug("Into save on Adapter MongoDB");
		estudiosRepositoryMongo.save(estudiosMapperMongo.fromDomainToAdapter(study));
		// El documento guardado solo trae los _id de las referencias; se relee para
		// devolver la persona y la profesión resueltas.
		return findById(study.getProfession().getIdentification(), study.getPerson().getIdentification());
	}

	@Override
	public Boolean delete(Integer professionId, Integer personId) {
		log.debug("Into delete on Adapter MongoDB");
		String id = id(professionId, personId);
		estudiosRepositoryMongo.deleteById(id);
		return !estudiosRepositoryMongo.existsById(id);
	}

	@Override
	public List<Study> find() {
		log.debug("Into find on Adapter MongoDB");
		return estudiosRepositoryMongo.findAll(BY_KEY).stream().map(estudiosMapperMongo::fromAdapterToDomain).toList();
	}

	@Override
	public List<Study> find(Integer page, Integer size) {
		log.debug("Into find page on Adapter MongoDB");
		return estudiosRepositoryMongo.findAll(PageRequest.of(page, size, BY_KEY)).stream()
				.map(estudiosMapperMongo::fromAdapterToDomain).toList();
	}

	@Override
	public Study findById(Integer professionId, Integer personId) {
		log.debug("Into findById on Adapter MongoDB");
		return estudiosRepositoryMongo.findById(id(professionId, personId)).map(estudiosMapperMongo::fromAdapterToDomain)
				.orElse(null);
	}

	@Override
	public Integer count() {
		log.debug("Into count on Adapter MongoDB");
		return Math.toIntExact(estudiosRepositoryMongo.count());
	}

	/** Mismo formato que EstudiosMapperMongo: la persona va primero. */
	private static String id(Integer professionId, Integer personId) {
		return personId + "-" + professionId;
	}
}
