package co.edu.javeriana.as.personapp.mongo.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument;

@Mapper
public class ProfesionMapperMongo {
	
	@Autowired
	private EstudiosMapperMongo estudiosMapperMongo;

	public ProfesionDocument fromDomainToAdapter(Profession profession) {
		ProfesionDocument profesionDocument = new ProfesionDocument();
		profesionDocument.setId(profession.getIdentification());
		profesionDocument.setNom(profession.getName());
		profesionDocument.setDes(validateDes(profession.getDescription()));
		profesionDocument.setEstudios(validateEstudios(profession.getStudies()));
		return profesionDocument;
	}

	/** Referencia a la profesión desde un estudio: solo la clave, porque @DocumentReference solo guarda el _id (#47). */
	public ProfesionDocument fromDomainToAdapterReference(Profession profession) {
		ProfesionDocument profesionDocument = new ProfesionDocument();
		profesionDocument.setId(profession.getIdentification());
		return profesionDocument;
	}

	private String validateDes(String description) {
		return description != null ? description : "";
	}

	private List<EstudiosDocument> validateEstudios(List<Study> studies) {
		return studies != null && !studies.isEmpty() ? studies.stream()
				.map(study -> estudiosMapperMongo.fromDomainToAdapter(study)).collect(Collectors.toList())
				: new ArrayList<EstudiosDocument>();
	}

	public Profession fromAdapterToDomain(ProfesionDocument profesionDocument) {
		Profession profession = fromAdapterToDomainBasic(profesionDocument);
		profession.setStudies(validateStudies(profesionDocument.getEstudios()));
		return profession;
	}

	/**
	 * Profesión sin estudios, para usarla dentro de un estudio. Evita la recursión
	 * mutua con el mapper de Estudios (#47). La colección queda en null.
	 */
	public Profession fromAdapterToDomainBasic(ProfesionDocument profesionDocument) {
		Profession profession = new Profession();
		profession.setIdentification(profesionDocument.getId());
		profession.setName(profesionDocument.getNom());
		profession.setDescription(validateDescription(profesionDocument.getDes()));
		return profession;
	}

	private String validateDescription(String des) {
		return des != null ? des : "";
	}

	private List<Study> validateStudies(List<EstudiosDocument> estudiosDocument) {
		return estudiosDocument != null && !estudiosDocument.isEmpty() ? estudiosDocument.stream()
				.map(estudio -> estudiosMapperMongo.fromAdapterToDomain(estudio)).collect(Collectors.toList())
				: new ArrayList<Study>();
	}
}
