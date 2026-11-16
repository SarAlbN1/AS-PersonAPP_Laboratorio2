package co.edu.javeriana.as.personapp.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.as.personapp.mariadb.entity.TelefonoEntity;

/** Teléfonos de MariaDB, con el número (VARCHAR) como clave. */
public interface TelefonoRepositoryMaria extends JpaRepository<TelefonoEntity, String> {

}
