package co.edu.javeriana.as.personapp.mariadb.mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntityPK;

@Mapper
public class EstudiosMapperMaria {

	@Autowired
	private PersonaMapperMaria personaMapperMaria;

	@Autowired
	private ProfesionMapperMaria profesionMapperMaria;

	public EstudiosEntity fromDomainToAdapter(Study study) {
		EstudiosEntityPK estudioPK = new EstudiosEntityPK();
		estudioPK.setCcPer(study.getPerson().getIdentification());
		estudioPK.setIdProf(study.getProfession().getIdentification());
		EstudiosEntity estudio = new EstudiosEntity();
		estudio.setEstudiosPK(estudioPK);
		estudio.setFecha(validateFecha(study.getGraduationDate()));
		estudio.setUniver(validateUniver(study.getUniversityName()));
		return estudio;
	}

	/** java.sql.Date conserva el día de la columna DATE sin pasar por una zona horaria (HU-023). */
	private Date validateFecha(LocalDate graduationDate) {
		return graduationDate != null ? java.sql.Date.valueOf(graduationDate) : null;
	}

	/** univer es opcional (VARCHAR(50) NULL): la ausencia se guarda como NULL, no como texto vacío (HU-023). */
	private String validateUniver(String universityName) {
		return universityName;
	}

	public Study fromAdapterToDomain(EstudiosEntity estudiosEntity) {
		Study study = new Study();
		study.setPerson(personaMapperMaria.fromAdapterToDomainBasic(estudiosEntity.getPersona()));
		study.setProfession(profesionMapperMaria.fromAdapterToDomainBasic(estudiosEntity.getProfesion()));
		study.setGraduationDate(validateGraduationDate(estudiosEntity.getFecha()));
		study.setUniversityName(validateUniversityName(estudiosEntity.getUniver()));
		return study;
	}

	/**
	 * JPA entrega la columna DATE como {@link java.sql.Date}, cuyo {@code toInstant()}
	 * lanza UnsupportedOperationException; ese tipo se convierte con {@code toLocalDate()}.
	 */
	private LocalDate validateGraduationDate(Date fecha) {
		if (fecha == null)
			return null;
		if (fecha instanceof java.sql.Date sqlDate)
			return sqlDate.toLocalDate();
		return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	private String validateUniversityName(String univer) {
		return univer;
	}
}