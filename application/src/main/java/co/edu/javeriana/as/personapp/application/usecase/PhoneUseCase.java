package co.edu.javeriana.as.personapp.application.usecase;

import java.util.List;
import java.util.regex.Pattern;

import co.edu.javeriana.as.personapp.application.port.in.PhoneInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import lombok.extern.slf4j.Slf4j;

/**
 * Casos de uso de Teléfono con las reglas comunes de HU-011 (persistencia §6.1).
 * Recibe los puertos de Teléfono y de Persona del mismo motor, porque el dueño se
 * comprueba en el motor elegido (C-03). El adaptador de entrada lo crea en cada
 * operación con los puertos de ese motor, sin estado compartido (ADR-002).
 */
@Slf4j
public class PhoneUseCase implements PhoneInputPort {

	public static final int MAX_COMPANY_LENGTH = 45;
	public static final int MAX_PAGE_SIZE = 100;
	private static final Pattern NUMBER = Pattern.compile("^[0-9]{1,15}$");

	private final PhoneOutputPort phonePersistence;
	private final PersonOutputPort personPersistence;

	public PhoneUseCase(PhoneOutputPort phonePersistence, PersonOutputPort personPersistence) {
		this.phonePersistence = phonePersistence;
		this.personPersistence = personPersistence;
	}

	@Override
	public Phone create(Phone phone) throws InvalidDataException, DuplicateException, ReferenceNotFoundException {
		validate(phone);
		if (phonePersistence.findById(phone.getNumber()) != null)
			throw new DuplicateException("Ya existe un teléfono con número " + phone.getNumber());
		phone.setOwner(existingOwner(phone));
		log.debug("Into create on Application Domain");
		return phonePersistence.save(phone);
	}

	@Override
	public Phone edit(String number, Phone phone)
			throws InvalidDataException, KeyMismatchException, NoExistException, ReferenceNotFoundException {
		validate(phone);
		if (!phone.getNumber().equals(number))
			throw new KeyMismatchException("El número del cuerpo (" + phone.getNumber()
					+ ") no coincide con el de la ruta (" + number + ")");
		if (phonePersistence.findById(number) == null)
			throw new NoExistException("No existe un teléfono con número " + number);
		// C-04: el teléfono puede cambiar de dueño si el nuevo existe.
		phone.setOwner(existingOwner(phone));
		return phonePersistence.save(phone);
	}

	@Override
	public Boolean drop(String number) throws InvalidDataException, NoExistException {
		validateKey(number);
		// C-05: un teléfono no tiene dependientes, se elimina sin restricción.
		if (phonePersistence.findById(number) == null)
			throw new NoExistException("No existe un teléfono con número " + number);
		return phonePersistence.delete(number);
	}

	@Override
	public List<Phone> findAll() {
		return phonePersistence.find();
	}

	@Override
	public List<Phone> findAll(Integer page, Integer size) throws InvalidDataException {
		if (page == null || size == null)
			throw new InvalidDataException("La página y el tamaño son obligatorios en el listado parcial");
		if (page < 0)
			throw new InvalidDataException("La página debe ser mayor o igual a 0");
		if (size < 1 || size > MAX_PAGE_SIZE)
			throw new InvalidDataException("El tamaño de página debe estar entre 1 y " + MAX_PAGE_SIZE);
		return phonePersistence.find(page, size);
	}

	@Override
	public Phone findOne(String number) throws InvalidDataException, NoExistException {
		validateKey(number);
		Phone phone = phonePersistence.findById(number);
		if (phone != null)
			return phone;
		throw new NoExistException("No existe un teléfono con número " + number);
	}

	@Override
	public Integer count() {
		return phonePersistence.count();
	}

	private Person existingOwner(Phone phone) throws ReferenceNotFoundException {
		Integer cc = phone.getOwner().getIdentification();
		Person owner = personPersistence.findById(cc);
		if (owner == null)
			throw new ReferenceNotFoundException("No existe una persona con cc " + cc + " para ser dueña del teléfono");
		return owner;
	}

	/** Una clave con formato inválido es ERR-01, no ERR-03 (API REST §3.1). */
	private static void validateKey(String number) throws InvalidDataException {
		if (number == null || !NUMBER.matcher(number).matches())
			throw new InvalidDataException("El número debe tener entre 1 y 15 dígitos");
	}

	private void validate(Phone phone) throws InvalidDataException {
		if (phone == null)
			throw new InvalidDataException("El teléfono es obligatorio");
		if (phone.getNumber() == null || !NUMBER.matcher(phone.getNumber()).matches())
			throw new InvalidDataException("El número debe tener entre 1 y 15 dígitos");
		String company = phone.getCompany() == null ? "" : phone.getCompany().trim();
		if (company.isEmpty() || company.length() > MAX_COMPANY_LENGTH)
			throw new InvalidDataException(
					"El operador debe tener entre 1 y " + MAX_COMPANY_LENGTH + " caracteres");
		phone.setCompany(company);
		if (phone.getOwner() == null || phone.getOwner().getIdentification() == null)
			throw new InvalidDataException("El dueño (cc) es obligatorio");
	}
}
