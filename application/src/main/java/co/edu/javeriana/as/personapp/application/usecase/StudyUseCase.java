package co.edu.javeriana.as.personapp.application.usecase;

import java.util.List;

import co.edu.javeriana.as.personapp.application.port.in.StudyInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import lombok.extern.slf4j.Slf4j;

/**
 * Casos de uso de Estudios con las reglas comunes de HU-011 (persistencia §6.1).
 * La identidad es el par (profesión, persona) del modelo base (C-01). Recibe los puertos
 * de Estudios, Persona y Profesión del mismo motor, porque las dos referencias se
 * comprueban en el motor elegido (C-03). El adaptador de entrada lo crea en cada
 * operación con los puertos de ese motor, sin estado compartido (ADR-002).
 */
@Slf4j
public class StudyUseCase implements StudyInputPort {

	public static final int MAX_UNIVERSITY_LENGTH = 50;
	public static final int MAX_PAGE_SIZE = 100;

	private final StudyOutputPort studyPersistence;
	private final PersonOutputPort personPersistence;
	private final ProfessionOutputPort professionPersistence;

	public StudyUseCase(StudyOutputPort studyPersistence, PersonOutputPort personPersistence,
			ProfessionOutputPort professionPersistence) {
		this.studyPersistence = studyPersistence;
		this.personPersistence = personPersistence;
		this.professionPersistence = professionPersistence;
	}

	@Override
	public Study create(Study study) throws InvalidDataException, DuplicateException, ReferenceNotFoundException {
		validate(study);
		Integer professionId = study.getProfession().getIdentification();
		Integer personId = study.getPerson().getIdentification();
		if (studyPersistence.findById(professionId, personId) != null)
			throw new DuplicateException(
					"Ya existe un estudio de la profesión " + professionId + " para la persona " + personId);
		Person person = personPersistence.findById(personId);
		if (person == null)
			throw new ReferenceNotFoundException("No existe una persona con cc " + personId);
		Profession profession = professionPersistence.findById(professionId);
		if (profession == null)
			throw new ReferenceNotFoundException("No existe una profesión con id " + professionId);
		study.setPerson(person);
		study.setProfession(profession);
		log.debug("Into create on Application Domain");
		return studyPersistence.save(study);
	}

	@Override
	public Study edit(Integer professionId, Integer personId, Study study)
			throws InvalidDataException, KeyMismatchException, NoExistException {
		validate(study);
		Integer bodyProfessionId = study.getProfession().getIdentification();
		Integer bodyPersonId = study.getPerson().getIdentification();
		if (!bodyProfessionId.equals(professionId) || !bodyPersonId.equals(personId))
			throw new KeyMismatchException("La clave del cuerpo (" + bodyProfessionId + ", " + bodyPersonId
					+ ") no coincide con la de la ruta (" + professionId + ", " + personId + ")");
		Study existing = studyPersistence.findById(professionId, personId);
		if (existing == null)
			throw new NoExistException(
					"No existe un estudio de la profesión " + professionId + " para la persona " + personId);
		// C-04: la clave no cambia; solo se actualizan la fecha y la universidad.
		existing.setGraduationDate(study.getGraduationDate());
		existing.setUniversityName(study.getUniversityName());
		return studyPersistence.save(existing);
	}

	@Override
	public Boolean drop(Integer professionId, Integer personId) throws NoExistException {
		// C-05: un estudio no tiene dependientes, se elimina sin restricción.
		if (studyPersistence.findById(professionId, personId) == null)
			throw new NoExistException(
					"No existe un estudio de la profesión " + professionId + " para la persona " + personId);
		return studyPersistence.delete(professionId, personId);
	}

	@Override
	public List<Study> findAll() {
		return studyPersistence.find();
	}

	@Override
	public List<Study> findAll(Integer page, Integer size) throws InvalidDataException {
		if (page == null || size == null)
			throw new InvalidDataException("La página y el tamaño son obligatorios en el listado parcial");
		if (page < 0)
			throw new InvalidDataException("La página debe ser mayor o igual a 0");
		if (size < 1 || size > MAX_PAGE_SIZE)
			throw new InvalidDataException("El tamaño de página debe estar entre 1 y " + MAX_PAGE_SIZE);
		return studyPersistence.find(page, size);
	}

	@Override
	public Study findOne(Integer professionId, Integer personId) throws NoExistException {
		Study study = studyPersistence.findById(professionId, personId);
		if (study != null)
			return study;
		throw new NoExistException(
				"No existe un estudio de la profesión " + professionId + " para la persona " + personId);
	}

	@Override
	public Integer count() {
		return studyPersistence.count();
	}

	private void validate(Study study) throws InvalidDataException {
		if (study == null)
			throw new InvalidDataException("El estudio es obligatorio");
		if (study.getProfession() == null || !validId(study.getProfession().getIdentification()))
			throw new InvalidDataException("El id de la profesión debe ser un entero mayor o igual a 1");
		if (study.getPerson() == null || !validId(study.getPerson().getIdentification()))
			throw new InvalidDataException("El cc de la persona debe ser un entero mayor o igual a 1");
		String university = study.getUniversityName() == null ? null : study.getUniversityName().trim();
		if (university != null && university.length() > MAX_UNIVERSITY_LENGTH)
			throw new InvalidDataException(
					"La universidad admite hasta " + MAX_UNIVERSITY_LENGTH + " caracteres");
		study.setUniversityName(university == null || university.isEmpty() ? null : university);
	}

	private static boolean validId(Integer id) {
		return id != null && id >= 1;
	}
}
