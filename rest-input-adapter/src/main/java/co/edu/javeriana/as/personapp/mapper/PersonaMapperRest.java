package co.edu.javeriana.as.personapp.mapper;

import java.util.Locale;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.model.request.PersonaRequest;
import co.edu.javeriana.as.personapp.model.response.PersonaResponse;

@Mapper
public class PersonaMapperRest {
	
	public PersonaResponse fromDomainToAdapterRestMaria(Person person) {
		return fromDomainToAdapterRest(person, "MariaDB");
	}
	public PersonaResponse fromDomainToAdapterRestMongo(Person person) {
		return fromDomainToAdapterRest(person, "MongoDB");
	}
	
	public PersonaResponse fromDomainToAdapterRest(Person person, String database) {
		return new PersonaResponse(
				person.getIdentification()+"", 
				person.getFirstName(), 
				person.getLastName(), 
				// Edad opcional: sin dato se devuelve null, no el texto "null".
				person.getAge() == null ? null : person.getAge().toString(), 
				person.getGender().toString(), 
				database,
				"OK");
	}

	/** Convierte el cuerpo al dominio; cualquier valor que no se pueda interpretar es ERR-01. */
	public Person fromAdapterToDomain(PersonaRequest request) throws InvalidDataException {
		if (request == null)
			throw new InvalidDataException("El cuerpo de la solicitud es obligatorio");
		Person person = new Person();
		person.setIdentification(toIdentification(request.getDni()));
		// Person no acepta null: sin nombre o apellido el setter lanzaría NPE y REST respondería 500.
		person.setFirstName(required(request.getFirstName(), "firstName"));
		person.setLastName(required(request.getLastName(), "lastName"));
		person.setGender(toGender(request.getSex()));
		person.setAge(toAge(request.getAge()));
		return person;
	}

	private Integer toIdentification(String dni) throws InvalidDataException {
		if (dni == null || dni.isBlank()) {
			throw new InvalidDataException("El dni es obligatorio");
		}
		return toInteger(dni, "dni");
	}

	private String required(String valor, String campo) throws InvalidDataException {
		if (valor == null || valor.isBlank())
			throw new InvalidDataException("El " + campo + " es obligatorio");
		return valor;
	}

	private Integer toInteger(String valor, String campo) throws InvalidDataException {
		try {
			return Integer.valueOf(valor.trim());
		} catch (NumberFormatException e) {
			throw new InvalidDataException("El " + campo + " '" + valor + "' no es un número entero válido");
		}
	}

	private Gender toGender(String sex) throws InvalidDataException {
		String value = sex == null ? "" : sex.trim().toUpperCase(Locale.ROOT);
		switch (value) {
		case "M":
		case "MALE":
			return Gender.MALE;
		case "F":
		case "FEMALE":
			return Gender.FEMALE;
		default:
			throw new InvalidDataException("El sexo '" + sex + "' no es válido; use M o F");
		}
	}

	private Integer toAge(String age) throws InvalidDataException {
		return age == null || age.isBlank() ? null : toInteger(age, "age");
	}

}
