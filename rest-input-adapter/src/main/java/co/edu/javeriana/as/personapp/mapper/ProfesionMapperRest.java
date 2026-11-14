package co.edu.javeriana.as.personapp.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.model.request.ProfesionRequest;
import co.edu.javeriana.as.personapp.model.response.ProfesionResponse;

@Mapper
public class ProfesionMapperRest {

	/** Copia los campos tal como llegan; la validación es del caso de uso (ERR-01). */
	public Profession fromAdapterToDomain(ProfesionRequest request) {
		Profession profession = new Profession();
		profession.setIdentification(request.id());
		profession.setName(request.name());
		profession.setDescription(request.description());
		return profession;
	}

	public ProfesionResponse fromDomainToAdapterRest(Profession profession) {
		return new ProfesionResponse(profession.getIdentification(), profession.getName(),
				profession.getDescription());
	}
}
