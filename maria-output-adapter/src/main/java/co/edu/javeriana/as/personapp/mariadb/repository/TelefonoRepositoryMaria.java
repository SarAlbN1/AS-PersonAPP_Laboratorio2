package co.edu.javeriana.as.personapp.mariadb.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.as.personapp.mariadb.entity.TelefonoEntity;

/** Teléfonos de MariaDB, con el número (VARCHAR) como clave. */
public interface TelefonoRepositoryMaria extends JpaRepository<TelefonoEntity, String> {

	/** Teléfonos de un dueño con el orden indicado (HU-033). */
	List<TelefonoEntity> findByDuenioCc(Integer cc, Sort sort);

}
