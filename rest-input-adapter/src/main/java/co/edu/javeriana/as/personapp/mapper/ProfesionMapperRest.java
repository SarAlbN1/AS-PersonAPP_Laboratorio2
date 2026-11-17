package co.edu.javeriana.as.personapp.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.model.request.ProfesionRequest;
import co.edu.javeriana.as.personapp.model.response.ProfesionResponse;

@Mapper
public class ProfesionMapperRest {

	/**
	 * Copia los campos que llegan; la validación es del caso de uso (ERR-01). Los
	 * setters de {@link Profession} rechazan null, así que un id o un nombre ausente
	 * queda sin asignar y el caso de uso lo reporta como dato obligatorio, en vez
	 * de un 500 por {@code NullPointerException}.
	 */
	public Profession fromAdapterToDomain(ProfesionRequest request) {
		Profession profession = new Profession();
		if (request.id() != null)
			profession.setIdentification(request.id());
		if (request.name() != null)
			profession.setName(request.name());
		profession.setDescription(request.description());
		return profession;
	}

	public ProfesionResponse fromDomainToAdapterRest(Profession profession) {
		return new ProfesionResponse(profession.getIdentification(), profession.getName(),
				profession.getDescription());
	}
}
