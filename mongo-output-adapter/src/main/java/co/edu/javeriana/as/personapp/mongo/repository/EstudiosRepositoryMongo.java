package co.edu.javeriana.as.personapp.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;

/** El _id es el texto "ccPersona-idProfesion" que arma EstudiosMapperMongo. */
public interface EstudiosRepositoryMongo extends MongoRepository<EstudiosDocument, String> {

}
