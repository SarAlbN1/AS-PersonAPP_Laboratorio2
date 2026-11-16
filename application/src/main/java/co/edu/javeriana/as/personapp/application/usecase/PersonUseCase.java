package co.edu.javeriana.as.personapp.application.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PersonInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.UseCase;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.domain.Study;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
public class PersonUseCase implements PersonInputPort {

	
	public static final int MAX_NAME_LENGTH = 45;

	private static final int PRIMERA_PAGINA = 0;
	private static final int TAMANIO_MINIMO = 1;
	private static final int TAMANIO_MAXIMO = 100;

	private PersonOutputPort personPersintence;
	
	public PersonUseCase(@Qualifier("personOutputAdapterMaria") PersonOutputPort personPersintence) {
		this.personPersintence=personPersintence;
	}
	
	@Override
	public void setPersintence(PersonOutputPort personPersintence) {
		this.personPersintence=personPersintence;
	}

	@Override
	public Person create(Person person) throws InvalidDataException, DuplicateException {
		validate(person);
		// C-02: el alta duplicada se rechaza antes de guardar, porque save actualiza
		// silenciosamente un registro existente en JPA y en Spring Data MongoDB.
		if (personPersintence.findById(person.getIdentification()) != null)
			throw new DuplicateException("Ya existe una persona con cc " + person.getIdentification());
		log.debug("Into create on Application Domain");
		return personPersintence.save(person);
	}

	@Override
	public Person edit(Integer identification, Person person)
			throws InvalidDataException, KeyMismatchException, NoExistException {
		validate(person);
		// C-04: la identidad no cambia al editar.
		if (!person.getIdentification().equals(identification))
			throw new KeyMismatchException("El cc del cuerpo (" + person.getIdentification()
					+ ") no coincide con el de la ruta (" + identification + ")");
		Person oldPerson = personPersintence.findById(identification);
		if (oldPerson != null)
			return personPersintence.save(person);
		throw new NoExistException(
				"No existe una persona con cc " + identification);
	}

	@Override
	public Boolean drop(Integer identification) throws NoExistException {
		Person oldPerson = personPersintence.findById(identification);
		if (oldPerson != null)
			return personPersintence.delete(identification);
		throw new NoExistException(
				"No existe una persona con cc " + identification);
	}

	@Override
	public List<Person> findAll() {
		log.info("Output: " + personPersintence.getClass());
		return personPersintence.find();
	}

	@Override
	public Person findOne(Integer identification) throws NoExistException {
		Person oldPerson = personPersintence.findById(identification);
		if (oldPerson != null)
			return oldPerson;
		throw new NoExistException("No existe una persona con cc " + identification);
	}

	@Override
	public List<Person> findAll(Integer page, Integer size) throws InvalidDataException {
		if (page == null || size == null)
			throw new InvalidDataException("The page and the size are required to list partially");
		if (page < PRIMERA_PAGINA)
			throw new InvalidDataException("The page " + page + " is invalid, it starts at " + PRIMERA_PAGINA);
		if (size < TAMANIO_MINIMO || size > TAMANIO_MAXIMO)
			throw new InvalidDataException(
					"The size " + size + " is invalid, it goes from " + TAMANIO_MINIMO + " to " + TAMANIO_MAXIMO);
		log.debug("Into findAll page " + page + " size " + size + " on Application Domain");
		return personPersintence.find(page, size);
	}

	@Override
	public Integer count() {
		log.debug("Into count on Application Domain");
		return personPersintence.count();
	}

	/**
	 * Validaciones comunes de Persona (validaciones y errores §2.1): cc entero desde 1,
	 * nombre y apellido de 1 a 45 caracteres tras recortar, género MALE o FEMALE —OTHER
	 * se rechaza por la decisión D-01, porque el ENUM de MariaDB no lo admite— y edad
	 * opcional no negativa.
	 */
	private void validate(Person person) throws InvalidDataException {
		if (person == null)
			throw new InvalidDataException("La persona es obligatoria");
		if (person.getIdentification() == null || person.getIdentification() < 1)
			throw new InvalidDataException("El cc debe ser un entero mayor o igual a 1");
		person.setFirstName(validName(person.getFirstName(), "nombre"));
		person.setLastName(validName(person.getLastName(), "apellido"));
		if (person.getGender() != Gender.MALE && person.getGender() != Gender.FEMALE)
			throw new InvalidDataException("El género debe ser MALE o FEMALE");
		if (person.getAge() != null && person.getAge() < 0)
			throw new InvalidDataException("La edad no puede ser negativa");
	}

	private String validName(String value, String campo) throws InvalidDataException {
		String limpio = value == null ? "" : value.trim();
		if (limpio.isEmpty() || limpio.length() > MAX_NAME_LENGTH)
			throw new InvalidDataException(
					"El " + campo + " debe tener entre 1 y " + MAX_NAME_LENGTH + " caracteres");
		return limpio;
	}

	@Override
	public List<Phone> getPhones(Integer identification) throws NoExistException {
		Person oldPerson = personPersintence.findById(identification);
		if (oldPerson != null)
			return oldPerson.getPhoneNumbers();
		throw new NoExistException(
				"No existe una persona con cc " + identification);
	}

	@Override
	public List<Study> getStudies(Integer identification) throws NoExistException {
		Person oldPerson = personPersintence.findById(identification);
		if (oldPerson != null)
			return oldPerson.getStudies();
		throw new NoExistException(
				"No existe una persona con cc " + identification);
	}
}
