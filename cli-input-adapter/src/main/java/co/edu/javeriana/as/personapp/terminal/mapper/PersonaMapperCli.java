package co.edu.javeriana.as.personapp.terminal.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.terminal.model.PersonaModelCli;

@Mapper
public class PersonaMapperCli {

	public PersonaModelCli fromDomainToAdapterCli(Person person) {
		return new PersonaModelCli(person.getIdentification(), person.getFirstName(), person.getLastName(),
				person.getGender().toString(), person.getAge());
	}

	/**
	 * Construye el dominio desde lo capturado en la CLI. El género admite M y F o
	 * MALE y FEMALE; cualquier otro valor es ERR-01, porque el ENUM de MariaDB solo
	 * acepta esos dos (decisión D-01).
	 */
	public Person fromAdapterCliToDomain(PersonaModelCli modelo) throws InvalidDataException {
		Person person = new Person();
		person.setIdentification(modelo.cc());
		person.setFirstName(modelo.nombre());
		person.setLastName(modelo.apellido());
		person.setGender(toGender(modelo.genero()));
		person.setAge(modelo.edad());
		return person;
	}

	private Gender toGender(String genero) throws InvalidDataException {
		String valor = genero == null ? "" : genero.trim().toUpperCase();
		return switch (valor) {
		case "M", "MALE" -> Gender.MALE;
		case "F", "FEMALE" -> Gender.FEMALE;
		default -> throw new InvalidDataException("El género '" + genero + "' no es válido; use M o F");
		};
	}
}
