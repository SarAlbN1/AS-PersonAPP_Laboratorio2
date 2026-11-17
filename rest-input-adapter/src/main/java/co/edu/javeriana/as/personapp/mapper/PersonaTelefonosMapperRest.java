package co.edu.javeriana.as.personapp.mapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.model.response.PersonaTelefonosResponse;
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;

@Mapper
public class PersonaTelefonosMapperRest {

	private final TelefonoMapperRest telefonoMapperRest;

	@Autowired
	public PersonaTelefonosMapperRest(TelefonoMapperRest telefonoMapperRest) {
		this.telefonoMapperRest = telefonoMapperRest;
	}

	/** La edad es opcional: sin dato sale como {@code null}, no como el texto "null". */
	public PersonaTelefonosResponse fromDomainToAdapterRest(Person person) {
		List<TelefonoResponse> phones = person.getPhoneNumbers() == null ? List.of()
				: person.getPhoneNumbers().stream().map(telefonoMapperRest::fromDomainToAdapterRest).toList();
		return new PersonaTelefonosResponse(String.valueOf(person.getIdentification()), person.getFirstName(),
				person.getLastName(), person.getAge() == null ? null : String.valueOf(person.getAge()),
				person.getGender() == null ? null : person.getGender().toString(), phones);
	}
}
