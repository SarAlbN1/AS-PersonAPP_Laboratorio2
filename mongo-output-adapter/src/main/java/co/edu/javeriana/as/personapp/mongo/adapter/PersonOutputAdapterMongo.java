package co.edu.javeriana.as.personapp.mongo.adapter;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoWriteException;

import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.mongo.document.PersonaDocument;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument;
import co.edu.javeriana.as.personapp.mongo.mapper.PersonaMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.PersonaRepositoryMongo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter("personOutputAdapterMongo")
public class PersonOutputAdapterMongo implements PersonOutputPort {
	
	@Autowired
    private PersonaRepositoryMongo personaRepositoryMongo;
	
	@Autowired
	private PersonaMapperMongo personaMapperMongo;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Person save(Person person) {
		log.debug("Into save on Adapter MongoDB");
		try {
			PersonaDocument persistedPersona = personaRepositoryMongo.save(personaMapperMongo.fromDomainToAdapter(person));
			return personaMapperMongo.fromAdapterToDomain(persistedPersona);
		} catch (MongoWriteException e) {
			log.warn(e.getMessage());
			return person;
		}		
	}

	@Override
	public Boolean delete(Integer identification) {
		log.debug("Into delete on Adapter MongoDB");
		personaRepositoryMongo.deleteById(identification);
		return personaRepositoryMongo.findById(identification).isEmpty();
	}

	@Override
	public List<Person> find() {
		log.debug("Into find on Adapter MongoDB");
		// C-06: el orden por clave ascendente lo pide el motor, no se ordena en memoria.
		return personaRepositoryMongo.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().map(personaMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public Person findById(Integer identification) {
		log.debug("Into findById on Adapter MongoDB");
		if (personaRepositoryMongo.findById(identification).isEmpty()) {
			return null;
		} else {
			return personaMapperMongo.fromAdapterToDomain(personaRepositoryMongo.findById(identification).get());
		}
	}

	@Override
	public Integer count() {
		log.debug("Into count on Adapter MongoDB");
		return (int) personaRepositoryMongo.count();
	}

	@Override
	public List<Person> find(Integer page, Integer size) {
		log.debug("Into find page on Adapter MongoDB");
		return personaRepositoryMongo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))).getContent().stream()
				.map(personaMapperMongo::fromAdapterToDomain).collect(Collectors.toList());
	}

	@Override
	public Boolean hasPhones(Integer identification) {
		log.debug("Into hasPhones on Adapter MongoDB");
		// primaryDuenio guarda solo la cédula del dueño (@DocumentReference).
		return mongoTemplate.exists(Query.query(Criteria.where("primaryDuenio").is(identification)),
				TelefonoDocument.class);
	}

	@Override
	public Boolean hasStudies(Integer identification) {
		log.debug("Into hasStudies on Adapter MongoDB");
		return mongoTemplate.exists(Query.query(Criteria.where("primaryPersona").is(identification)),
				EstudiosDocument.class);
	}
}
