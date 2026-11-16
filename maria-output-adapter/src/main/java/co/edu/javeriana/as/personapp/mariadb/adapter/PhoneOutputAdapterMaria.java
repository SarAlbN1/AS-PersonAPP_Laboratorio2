package co.edu.javeriana.as.personapp.mariadb.adapter;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mariadb.entity.TelefonoEntity;
import co.edu.javeriana.as.personapp.mariadb.mapper.TelefonoMapperMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.TelefonoRepositoryMaria;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistencia de Teléfono en MariaDB. El dueño se escribe solo con su cédula y la
 * FK `duenio` del DDL lo exige en el motor; se devuelve sin sus colecciones (C-10).
 */
@Slf4j
@Adapter("phoneOutputAdapterMaria")
@Transactional
public class PhoneOutputAdapterMaria implements PhoneOutputPort {

	/** Orden de C-06: número como texto, ascendente. */
	private static final Sort BY_NUMBER = Sort.by(Sort.Direction.ASC, "num");

	@Autowired
	private TelefonoRepositoryMaria telefonoRepositoryMaria;

	@Autowired
	private TelefonoMapperMaria telefonoMapperMaria;

	@Override
	public Phone save(Phone phone) {
		log.debug("Into save on Adapter MariaDB");
		TelefonoEntity persisted = telefonoRepositoryMaria.save(telefonoMapperMaria.fromDomainToAdapter(phone));
		return telefonoMapperMaria.fromAdapterToDomain(persisted);
	}

	@Override
	public Boolean delete(String number) {
		log.debug("Into delete on Adapter MariaDB");
		telefonoRepositoryMaria.deleteById(number);
		return !telefonoRepositoryMaria.existsById(number);
	}

	@Override
	public List<Phone> find() {
		log.debug("Into find on Adapter MariaDB");
		return telefonoRepositoryMaria.findAll(BY_NUMBER).stream().map(telefonoMapperMaria::fromAdapterToDomain)
				.toList();
	}

	@Override
	public List<Phone> find(Integer page, Integer size) {
		log.debug("Into find page on Adapter MariaDB");
		return telefonoRepositoryMaria.findAll(PageRequest.of(page, size, BY_NUMBER)).stream()
				.map(telefonoMapperMaria::fromAdapterToDomain).toList();
	}

	@Override
	public Phone findById(String number) {
		log.debug("Into findById on Adapter MariaDB");
		return telefonoRepositoryMaria.findById(number).map(telefonoMapperMaria::fromAdapterToDomain).orElse(null);
	}

	@Override
	public Integer count() {
		log.debug("Into count on Adapter MariaDB");
		return Math.toIntExact(telefonoRepositoryMaria.count());
	}
}
