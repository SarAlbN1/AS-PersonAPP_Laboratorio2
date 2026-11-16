package co.edu.javeriana.as.personapp.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.javeriana.as.personapp.mariadb.entity.PersonaEntity;

public interface PersonaRepositoryMaria extends JpaRepository<PersonaEntity, Integer> {

	/** Indica si la persona tiene teléfonos, sin cargar la colección (C-05). */
	@Query("SELECT COUNT(t) > 0 FROM TelefonoEntity t WHERE t.duenio.cc = :cc")
	boolean hasPhones(@Param("cc") Integer cc);

	/** Indica si la persona tiene estudios, sin cargar la colección (C-05). */
	@Query("SELECT COUNT(e) > 0 FROM EstudiosEntity e WHERE e.estudiosEntityPK.ccPer = :cc")
	boolean hasStudies(@Param("cc") Integer cc);
}
