package co.edu.javeriana.as.personapp.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument;

/** Teléfonos de MongoDB, con el número (String) como `_id`. */
public interface TelefonoRepositoryMongo extends MongoRepository<TelefonoDocument, String> {

}
