package co.edu.javeriana.as.personapp.mapper;

import java.util.Locale;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
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
				person.getAge()+"", 
				person.getGender().toString(), 
				database,
				"OK");
	}

	public Person fromAdapterToDomain(PersonaRequest request) {
		Person person = new Person();
		person.setIdentification(toIdentification(request.getDni()));
		person.setFirstName(request.getFirstName());
		person.setLastName(request.getLastName());
		person.setGender(toGender(request.getSex()));
		person.setAge(toAge(request.getAge()));
		return person;
	}

	private Integer toIdentification(String dni) {
		if (dni == null || dni.isBlank()) {
			throw new IllegalArgumentException("El dni es obligatorio.");
		}
		return Integer.valueOf(dni.trim());
	}

	private Gender toGender(String sex) {
		String value = sex == null ? "" : sex.trim().toUpperCase(Locale.ROOT);
		switch (value) {
		case "M":
		case "MALE":
			return Gender.MALE;
		case "F":
		case "FEMALE":
			return Gender.FEMALE;
		default:
			throw new IllegalArgumentException("Sexo no admitido: " + sex + ". Use M o F.");
		}
	}

	private Integer toAge(String age) {
		return age == null || age.isBlank() ? null : Integer.valueOf(age.trim());
	}

}
