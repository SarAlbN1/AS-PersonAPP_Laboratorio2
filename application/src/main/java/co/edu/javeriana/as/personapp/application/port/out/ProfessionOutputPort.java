package co.edu.javeriana.as.personapp.application.port.out;

import java.util.List;

import co.edu.javeriana.as.personapp.common.annotations.Port;
import co.edu.javeriana.as.personapp.domain.Profession;

@Port
public interface ProfessionOutputPort {
	public Profession save(Profession profession);
	public Boolean delete(Integer identification);
	/** Todas las profesiones ordenadas por id ascendente (C-06). */
	public List<Profession> find();
	/** Página desde 0 con el orden de C-06, pedido al motor (C-07). */
	public List<Profession> find(int page, int size);
	public Profession findById(Integer identification);
	/** Total calculado por el motor (C-08). */
	public Integer count();
	/** Indica si algún estudio referencia la profesión (C-05). */
	public Boolean hasStudies(Integer identification);
}
