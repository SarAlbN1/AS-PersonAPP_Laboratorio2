package co.edu.javeriana.as.personapp.mariadb.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.ProfesionEntity;

@Mapper
public class ProfesionMapperMaria {

	@Autowired
	private EstudiosMapperMaria estudiosMapperMaria;

	public ProfesionEntity fromDomainToAdapter(Profession profession) {
		ProfesionEntity profesionEntity = new ProfesionEntity();
		profesionEntity.setId(profession.getIdentification());
		profesionEntity.setNom(profession.getName());
		profesionEntity.setDes(validateDes(profession.getDescription()));
		profesionEntity.setEstudios(validateEstudios(profession.getStudies()));
		return profesionEntity;
	}

	/** Referencia a la profesión desde un estudio: solo la clave, que es lo que usa la FK (#47). */
	public ProfesionEntity fromDomainToAdapterReference(Profession profession) {
		ProfesionEntity profesionEntity = new ProfesionEntity();
		profesionEntity.setId(profession.getIdentification());
		return profesionEntity;
	}

	private String validateDes(String description) {
		return description != null ? description : "";
	}

	private List<EstudiosEntity> validateEstudios(List<Study> studies) {
		return studies != null && !studies.isEmpty() ? studies.stream()
				.map(study -> estudiosMapperMaria.fromDomainToAdapter(study)).collect(Collectors.toList())
				: new ArrayList<EstudiosEntity>();
	}

	public Profession fromAdapterToDomain(ProfesionEntity profesionEntity) {
		Profession profession = fromAdapterToDomainBasic(profesionEntity);
		profession.setStudies(validateStudies(profesionEntity.getEstudios()));
		return profession;
	}

	/**
	 * Profesión sin estudios, para usarla dentro de un estudio. Evita la recursión
	 * mutua con el mapper de Estudios (#47). La colección queda en null.
	 */
	public Profession fromAdapterToDomainBasic(ProfesionEntity profesionEntity) {
		Profession profession = new Profession();
		profession.setIdentification(profesionEntity.getId());
		profession.setName(profesionEntity.getNom());
		profession.setDescription(validateDescription(profesionEntity.getDes()));
		return profession;
	}

	private String validateDescription(String des) {
		return des != null ? des : "";
	}

	private List<Study> validateStudies(List<EstudiosEntity> estudiosEntity) {
		return estudiosEntity != null && !estudiosEntity.isEmpty() ? estudiosEntity.stream()
				.map(estudio -> estudiosMapperMaria.fromAdapterToDomain(estudio)).collect(Collectors.toList())
				: new ArrayList<Study>();
	}
}
