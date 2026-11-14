package co.edu.javeriana.as.personapp.application.port.in;

import java.util.List;

import co.edu.javeriana.as.personapp.common.annotations.Port;
import co.edu.javeriana.as.personapp.common.exceptions.DuplicateException;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidDataException;
import co.edu.javeriana.as.personapp.common.exceptions.KeyMismatchException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.exceptions.ReferenceNotFoundException;
import co.edu.javeriana.as.personapp.domain.Study;

/**
 * Estudios se identifica por el par (profesión, persona), en el orden de la ruta REST
 * {@code /estudio/{idProfesion}/{ccPersona}} (C-01).
 */
@Port
public interface StudyInputPort {

	public Study create(Study study) throws InvalidDataException, DuplicateException, ReferenceNotFoundException;

	/** Solo cambian la fecha y la universidad (C-04). */
	public Study edit(Integer professionId, Integer personId, Study study)
			throws InvalidDataException, KeyMismatchException, NoExistException;

	public Boolean drop(Integer professionId, Integer personId) throws NoExistException;

	public List<Study> findAll();

	/** Listado parcial (C-07): página desde 0 y tamaño de 1 a 100, con el orden de C-06. */
	public List<Study> findAll(Integer page, Integer size) throws InvalidDataException;

	public Study findOne(Integer professionId, Integer personId) throws NoExistException;

	public Integer count();
}
