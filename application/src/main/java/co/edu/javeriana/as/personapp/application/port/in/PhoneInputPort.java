package co.edu.javeriana.as.personapp.application.port.in;

import java.util.List;

import co.edu.javeriana.as.personapp.common.annotations.Port;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Phone;

@Port
public interface PhoneInputPort {

	public Phone create(Phone phone) throws InvalidDataException, DuplicateException, ReferenceNotFoundException;

	public Phone edit(String number, Phone phone)
			throws InvalidDataException, KeyMismatchException, NoExistException, ReferenceNotFoundException;

	public Boolean drop(String number) throws InvalidDataException, NoExistException;

	public List<Phone> findAll();

	/** Listado parcial (C-07): página desde 0 y tamaño de 1 a 100, con el orden de C-06. */
	public List<Phone> findAll(Integer page, Integer size) throws InvalidDataException;

	public Phone findOne(String number) throws InvalidDataException, NoExistException;

	public Integer count();
}
