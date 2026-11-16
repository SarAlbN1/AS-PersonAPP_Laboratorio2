package co.edu.javeriana.as.personapp.application.port.out;

import java.util.List;

import co.edu.javeriana.as.personapp.common.annotations.Port;
import co.edu.javeriana.as.personapp.domain.Phone;

@Port
public interface PhoneOutputPort {
	public Phone save(Phone phone);
	public Boolean delete(String number);
	/** Todos los teléfonos ordenados por número como texto, ascendente (C-06). */
	public List<Phone> find();
	/** Página desde 0 con el orden de C-06, pedido al motor (C-07). */
	public List<Phone> find(Integer page, Integer size);
	/** El teléfono con ese número, o null si no existe en el motor (C-10). */
	public Phone findById(String number);
	/** Total calculado por el motor (C-08). */
	public Integer count();
}
