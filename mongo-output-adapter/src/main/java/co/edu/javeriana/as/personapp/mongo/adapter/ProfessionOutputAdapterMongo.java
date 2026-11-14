package co.edu.javeriana.as.personapp.mongo.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument;
import co.edu.javeriana.as.personapp.mongo.mapper.ProfesionMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.ProfesionRepositoryMongo;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistencia de Profesión en MongoDB. Devuelve las profesiones sin sus
 * estudios (C-10). Los errores de escritura se propagan: no se devuelve la
 * entrada como si se hubiera guardado.
 */
@Slf4j
@Adapter("professionOutputAdapterMongo")
public class ProfessionOutputAdapterMongo implements ProfessionOutputPort {

	/** Orden de C-06 pedido al motor: MongoDB devuelve el orden de inserción si no se indica. */
	private static final Sort BY_ID = Sort.by(Sort.Direction.ASC, "_id");

	@Autowired
	private ProfesionRepositoryMongo profesionRepositoryMongo;

	@Autowired
	private ProfesionMapperMongo profesionMapperMongo;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public Profession save(Profession profession) {
		log.debug("Into save on Adapter MongoDB");
		ProfesionDocument persisted = profesionRepositoryMongo.save(profesionMapperMongo.fromDomainToAdapter(profession));
		return profesionMapperMongo.fromAdapterToDomainBasic(persisted);
	}

	@Override
	public Boolean delete(Integer identification) {
		log.debug("Into delete on Adapter MongoDB");
		profesionRepositoryMongo.deleteById(identification);
		return !profesionRepositoryMongo.existsById(identification);
	}

	@Override
	public List<Profession> find() {
		log.debug("Into find on Adapter MongoDB");
		return profesionRepositoryMongo.findAll(BY_ID).stream().map(profesionMapperMongo::fromAdapterToDomainBasic)
				.toList();
	}

	@Override
	public List<Profession> find(Integer page, Integer size) {
		log.debug("Into find page on Adapter MongoDB");
		return profesionRepositoryMongo.findAll(PageRequest.of(page, size, BY_ID)).stream()
				.map(profesionMapperMongo::fromAdapterToDomainBasic).toList();
	}

	@Override
	public Profession findById(Integer identification) {
		log.debug("Into findById on Adapter MongoDB");
		return profesionRepositoryMongo.findById(identification).map(profesionMapperMongo::fromAdapterToDomainBasic)
				.orElse(null);
	}

	@Override
	public Integer count() {
		log.debug("Into count on Adapter MongoDB");
		return Math.toIntExact(profesionRepositoryMongo.count());
	}

	@Override
	public Boolean hasStudies(Integer identification) {
		log.debug("Into hasStudies on Adapter MongoDB");
		// @DocumentReference guarda en primaryProfesion solo el _id de la profesión.
		return mongoTemplate.exists(Query.query(Criteria.where("primaryProfesion").is(identification)),
				EstudiosDocument.class);
	}
}
