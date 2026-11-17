package co.edu.javeriana.as.personapp.terminal.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.terminal.model.TelefonoModelCli;

@Mapper
public class TelefonoMapperCli {

	public TelefonoModelCli fromDomainToAdapterCli(Phone phone) {
		Integer cedula = phone.getOwner() == null ? null : phone.getOwner().getIdentification();
		return new TelefonoModelCli(phone.getNumber(), phone.getCompany(), cedula);
	}

	/** Los setters de {@link Phone} rechazan null: un campo ausente queda sin asignar (ERR-01). */
	public Phone fromAdapterCliToDomain(TelefonoModelCli telefono) {
		Phone phone = new Phone();
		if (telefono.numero() != null)
			phone.setNumber(telefono.numero());
		if (telefono.operador() != null)
			phone.setCompany(telefono.operador());
		if (telefono.cedulaDueno() != null) {
			Person owner = new Person();
			owner.setIdentification(telefono.cedulaDueno());
			phone.setOwner(owner);
		}
		return phone;
	}
}
