package co.edu.javeriana.as.personapp.application.port.in;

import java.util.List;

import co.edu.javeriana.as.personapp.common.annotations.Port;
import co.edu.javeriana.as.personapp.common.exceptions.DependentRecordsException;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Profession;

@Port
public interface ProfessionInputPort {

	public Profession create(Profession profession) throws InvalidDataException, DuplicateException;

	public Profession edit(Integer identification, Profession profession)
			throws InvalidDataException, KeyMismatchException, NoExistException;

	public Boolean drop(Integer identification) throws NoExistException, DependentRecordsException;

	public List<Profession> findAll();

	public List<Profession> findPage(int page, int size) throws InvalidDataException;

	public Profession findOne(Integer identification) throws NoExistException;

	public Integer count();
}
