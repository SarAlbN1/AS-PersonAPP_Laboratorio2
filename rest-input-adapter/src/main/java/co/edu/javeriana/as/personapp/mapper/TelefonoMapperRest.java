package co.edu.javeriana.as.personapp.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.model.request.TelefonoRequest;
import co.edu.javeriana.as.personapp.model.response.TelefonoResponse;

@Mapper
public class TelefonoMapperRest {

	/**
	 * Copia los campos que llegan; la validación es del caso de uso (ERR-01). Los
	 * setters de {@link Phone} rechazan null, así que un campo ausente queda sin
	 * asignar y el caso de uso lo reporta como dato obligatorio.
	 */
	public Phone fromAdapterToDomain(TelefonoRequest request) {
		Phone phone = new Phone();
		if (request.number() != null)
			phone.setNumber(request.number());
		if (request.company() != null)
			phone.setCompany(request.company());
		if (request.ownerId() != null) {
			Person owner = new Person();
			owner.setIdentification(request.ownerId());
			phone.setOwner(owner);
		}
		return phone;
	}

	public TelefonoResponse fromDomainToAdapterRest(Phone phone) {
		Integer ownerId = phone.getOwner() == null ? null : phone.getOwner().getIdentification();
		return new TelefonoResponse(phone.getNumber(), phone.getCompany(), ownerId);
	}
}
