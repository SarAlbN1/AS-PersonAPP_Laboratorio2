package co.edu.javeriana.as.personapp.mongo.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument;
import co.edu.javeriana.as.personapp.mongo.mapper.TelefonoMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.TelefonoRepositoryMongo;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistencia de Teléfono en MongoDB. MongoDB no tiene FK: `primaryDuenio` guarda
 * solo la cédula, y que el dueño exista lo comprueba el caso de uso en este mismo
 * motor (C-03). Los errores de escritura se propagan, no se devuelve la entrada
 * como si se hubiera guardado.
 */
@Slf4j
@Adapter("phoneOutputAdapterMongo")
public class PhoneOutputAdapterMongo implements PhoneOutputPort {

	/** Orden de C-06 pedido al motor: `_id` es texto, así que el orden es de texto. */
	private static final Sort BY_NUMBER = Sort.by(Sort.Direction.ASC, "id");

	@Autowired
	private TelefonoRepositoryMongo telefonoRepositoryMongo;

	@Autowired
	private TelefonoMapperMongo telefonoMapperMongo;

	@Override
	public Phone save(Phone phone) {
		log.debug("Into save on Adapter MongoDB");
		TelefonoDocument persisted = telefonoRepositoryMongo.save(telefonoMapperMongo.fromDomainToAdapter(phone));
		// save devuelve el mismo documento, con el dueño reducido a su cédula; se relee
		// para que @DocumentReference resuelva el dueño guardado en el motor.
		return findById(persisted.getId());
	}

	@Override
	public Boolean delete(String number) {
		log.debug("Into delete on Adapter MongoDB");
		telefonoRepositoryMongo.deleteById(number);
		return !telefonoRepositoryMongo.existsById(number);
	}

	@Override
	public List<Phone> find() {
		log.debug("Into find on Adapter MongoDB");
		return telefonoRepositoryMongo.findAll(BY_NUMBER).stream().map(telefonoMapperMongo::fromAdapterToDomain)
				.toList();
	}

	@Override
	public List<Phone> find(Integer page, Integer size) {
		log.debug("Into find page on Adapter MongoDB");
		return telefonoRepositoryMongo.findAll(PageRequest.of(page, size, BY_NUMBER)).stream()
				.map(telefonoMapperMongo::fromAdapterToDomain).toList();
	}

	@Override
	public Phone findById(String number) {
		log.debug("Into findById on Adapter MongoDB");
		return telefonoRepositoryMongo.findById(number).map(telefonoMapperMongo::fromAdapterToDomain).orElse(null);
	}

	@Override
	public Integer count() {
		log.debug("Into count on Adapter MongoDB");
		return Math.toIntExact(telefonoRepositoryMongo.count());
	}
}
