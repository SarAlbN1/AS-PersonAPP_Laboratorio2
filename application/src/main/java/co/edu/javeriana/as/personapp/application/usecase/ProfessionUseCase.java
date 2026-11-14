package co.edu.javeriana.as.personapp.application.usecase;

import java.util.List;

import co.edu.javeriana.as.personapp.application.port.in.ProfessionInputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Profession;
import lombok.extern.slf4j.Slf4j;

/**
 * Casos de uso de Profesión con las reglas comunes de HU-011 (persistencia §6.1).
 * El adaptador de entrada lo crea en cada operación con el puerto del motor elegido,
 * así que no guarda estado compartido entre peticiones (ADR-002).
 */
@Slf4j
public class ProfessionUseCase implements ProfessionInputPort {

	public static final int MAX_NAME_LENGTH = 90;
	public static final int MAX_PAGE_SIZE = 100;

	private final ProfessionOutputPort professionPersistence;

	public ProfessionUseCase(ProfessionOutputPort professionPersistence) {
		this.professionPersistence = professionPersistence;
	}

	@Override
	public Profession create(Profession profession) throws InvalidDataException, DuplicateException {
		validate(profession);
		if (professionPersistence.findById(profession.getIdentification()) != null)
			throw new DuplicateException(
					"Ya existe una profesión con id " + profession.getIdentification());
		log.debug("Into create on Application Domain");
		return professionPersistence.save(profession);
	}

	@Override
	public Profession edit(Integer identification, Profession profession)
			throws InvalidDataException, KeyMismatchException, NoExistException {
		validate(profession);
		if (!profession.getIdentification().equals(identification))
			throw new KeyMismatchException("El id del cuerpo (" + profession.getIdentification()
					+ ") no coincide con el de la ruta (" + identification + ")");
		if (professionPersistence.findById(identification) == null)
			throw new NoExistException("No existe una profesión con id " + identification);
		return professionPersistence.save(profession);
	}

	@Override
	public Boolean drop(Integer identification) throws NoExistException, DependentRecordsException {
		if (professionPersistence.findById(identification) == null)
			throw new NoExistException("No existe una profesión con id " + identification);
		if (professionPersistence.hasStudies(identification))
			throw new DependentRecordsException(
					"La profesión con id " + identification + " tiene estudios asociados");
		return professionPersistence.delete(identification);
	}

	@Override
	public List<Profession> findAll() {
		return professionPersistence.find();
	}

	@Override
	public List<Profession> findAll(Integer page, Integer size) throws InvalidDataException {
		if (page == null || size == null)
			throw new InvalidDataException("La página y el tamaño son obligatorios en el listado parcial");
		if (page < 0)
			throw new InvalidDataException("La página debe ser mayor o igual a 0");
		if (size < 1 || size > MAX_PAGE_SIZE)
			throw new InvalidDataException("El tamaño de página debe estar entre 1 y " + MAX_PAGE_SIZE);
		return professionPersistence.find(page, size);
	}

	@Override
	public Profession findOne(Integer identification) throws NoExistException {
		Profession profession = professionPersistence.findById(identification);
		if (profession != null)
			return profession;
		throw new NoExistException("No existe una profesión con id " + identification);
	}

	@Override
	public Integer count() {
		return professionPersistence.count();
	}

	private void validate(Profession profession) throws InvalidDataException {
		if (profession == null)
			throw new InvalidDataException("La profesión es obligatoria");
		if (profession.getIdentification() == null || profession.getIdentification() < 1)
			throw new InvalidDataException("El id de la profesión debe ser un entero mayor o igual a 1");
		String name = profession.getName() == null ? "" : profession.getName().trim();
		if (name.isEmpty() || name.length() > MAX_NAME_LENGTH)
			throw new InvalidDataException(
					"El nombre de la profesión debe tener entre 1 y " + MAX_NAME_LENGTH + " caracteres");
		profession.setName(name);
	}
}
