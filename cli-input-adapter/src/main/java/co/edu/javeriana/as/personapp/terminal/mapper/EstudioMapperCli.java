package co.edu.javeriana.as.personapp.terminal.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.terminal.model.EstudioModelCli;

@Mapper
public class EstudioMapperCli {

	public EstudioModelCli fromDomainToAdapterCli(Study study) {
		Integer idProfesion = study.getProfession() == null ? null : study.getProfession().getIdentification();
		Integer ccPersona = study.getPerson() == null ? null : study.getPerson().getIdentification();
		return new EstudioModelCli(idProfesion, ccPersona, study.getGraduationDate(), study.getUniversityName());
	}

	/** Los setters de la clave de {@link Study} rechazan null: una parte ausente queda sin asignar (ERR-01). */
	public Study fromAdapterCliToDomain(EstudioModelCli estudio) {
		Study study = new Study();
		if (estudio.idProfesion() != null) {
			Profession profession = new Profession();
			profession.setIdentification(estudio.idProfesion());
			study.setProfession(profession);
		}
		if (estudio.ccPersona() != null) {
			Person person = new Person();
			person.setIdentification(estudio.ccPersona());
			study.setPerson(person);
		}
		study.setGraduationDate(estudio.fecha());
		study.setUniversityName(estudio.universidad());
		return study;
	}
}
