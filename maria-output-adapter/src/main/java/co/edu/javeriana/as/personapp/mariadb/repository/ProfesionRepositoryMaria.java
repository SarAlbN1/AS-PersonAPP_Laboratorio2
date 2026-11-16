package co.edu.javeriana.as.personapp.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.as.personapp.mariadb.entity.ProfesionEntity;

public interface ProfesionRepositoryMaria extends JpaRepository<ProfesionEntity, Integer> {

	/** Indica si algún estudio referencia la profesión, sin cargar la colección (C-05). */
	@Query("SELECT COUNT(e) > 0 FROM EstudiosEntity e WHERE e.estudiosEntityPK.idProf = :id")
	boolean hasStudies(@Param("id") Integer id);
}
