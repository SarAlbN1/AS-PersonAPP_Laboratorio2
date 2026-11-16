package co.edu.javeriana.as.personapp.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.model.request.EstudioRequest;
import co.edu.javeriana.as.personapp.model.response.EstudioResponse;

@Mapper
public class EstudioMapperRest {

	/**
	 * Copia los campos que llegan; la validación es del caso de uso (ERR-01). Los
	 * setters de la persona y la profesión de {@link Study} rechazan null, así que
	 * una parte de la clave ausente queda sin asignar y el caso de uso la reporta.
	 */
	public Study fromAdapterToDomain(EstudioRequest request) {
		Study study = new Study();
		if (request.professionId() != null) {
			Profession profession = new Profession();
			profession.setIdentification(request.professionId());
			study.setProfession(profession);
		}
		if (request.personId() != null) {
			Person person = new Person();
			person.setIdentification(request.personId());
			study.setPerson(person);
		}
		study.setGraduationDate(request.graduationDate());
		study.setUniversityName(request.universityName());
		return study;
	}

	public EstudioResponse fromDomainToAdapterRest(Study study) {
		Integer professionId = study.getProfession() == null ? null : study.getProfession().getIdentification();
		Integer personId = study.getPerson() == null ? null : study.getPerson().getIdentification();
		return new EstudioResponse(professionId, personId, study.getGraduationDate(), study.getUniversityName());
	}
}
