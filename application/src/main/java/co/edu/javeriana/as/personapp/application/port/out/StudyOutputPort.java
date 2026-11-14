package co.edu.javeriana.as.personapp.application.port.out;

import java.util.List;

import co.edu.javeriana.as.personapp.common.annotations.Port;
import co.edu.javeriana.as.personapp.domain.Study;

@Port
public interface StudyOutputPort {
	public Study save(Study study);
	public Boolean delete(Integer professionId, Integer personId);
	/** Todos los estudios ordenados por id de profesión y luego por cc de persona (C-06). */
	public List<Study> find();
	/** Página desde 0 con el orden de C-06, pedido al motor (C-07). */
	public List<Study> find(Integer page, Integer size);
	/** El estudio con esa clave completa, o null si no existe en el motor (C-10). */
	public Study findById(Integer professionId, Integer personId);
	/** Total calculado por el motor (C-08). */
	public Integer count();
}
