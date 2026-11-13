package co.edu.javeriana.as.personapp.mariadb.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mariadb.entity.ProfesionEntity;
import co.edu.javeriana.as.personapp.mariadb.mapper.ProfesionMapperMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.ProfesionRepositoryMaria;

/** HU-017: el adaptador pide al motor el orden, la página y el conteo (C-06 a C-08). */
@ExtendWith(MockitoExtension.class)
class ProfessionOutputAdapterMariaTest {

	@Mock
	private ProfesionRepositoryMaria repository;

	@Spy
	private ProfesionMapperMaria mapper;

	@InjectMocks
	private ProfessionOutputAdapterMaria adapter;

	private static ProfesionEntity entity(int id, String nom) {
		ProfesionEntity e = new ProfesionEntity(id, nom);
		e.setDes("d" + id);
		return e;
	}

	@Test
	void listarTodosPideOrdenPorIdAlMotor() {
		when(repository.findAll(any(Sort.class))).thenReturn(List.of(entity(1, "A"), entity(2, "B")));
		List<Profession> result = adapter.find();
		ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
		verify(repository).findAll(sort.capture());
		assertEquals(Sort.by(Sort.Direction.ASC, "id"), sort.getValue());
		assertEquals(List.of(1, 2), result.stream().map(Profession::getIdentification).toList());
		assertNull(result.get(0).getStudies(), "C-10: no se cargan los estudios");
	}

	@Test
	void listarParcialPasaPaginaTamanoYOrden() {
		when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity(3, "C"))));
		List<Profession> result = adapter.find(1, 2);
		ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(repository).findAll(pageable.capture());
		assertEquals(1, pageable.getValue().getPageNumber());
		assertEquals(2, pageable.getValue().getPageSize());
		assertEquals(Sort.by(Sort.Direction.ASC, "id"), pageable.getValue().getSort());
		assertEquals(1, result.size());
	}

	@Test
	void listarVacioDevuelveListaVacia() {
		when(repository.findAll(any(Sort.class))).thenReturn(List.of());
		assertTrue(adapter.find().isEmpty());
	}

	@Test
	void consultarInexistenteDevuelveNull() {
		when(repository.findById(9)).thenReturn(Optional.empty());
		assertNull(adapter.findById(9));
	}

	@Test
	void consultarExistenteSinEstudios() {
		when(repository.findById(1)).thenReturn(Optional.of(entity(1, "A")));
		Profession p = adapter.findById(1);
		assertEquals("A", p.getName());
		assertEquals("d1", p.getDescription());
		assertNull(p.getStudies());
	}

	@Test
	void contarUsaCountDelMotor() {
		when(repository.count()).thenReturn(0L);
		assertEquals(0, adapter.count());
		verify(repository).count();
	}

	@Test
	void eliminarConfirmaQueYaNoExiste() {
		when(repository.existsById(1)).thenReturn(false);
		assertTrue(adapter.delete(1));
		verify(repository).deleteById(1);
	}

	@Test
	void guardarDevuelveLoPersistido() {
		when(repository.save(any(ProfesionEntity.class))).thenAnswer(i -> i.getArgument(0));
		Profession in = new Profession(5, "E", "desc", null);
		Profession out = adapter.save(in);
		assertEquals(5, out.getIdentification());
		assertEquals("desc", out.getDescription());
	}

	@Test
	void dependientesSeConsultanEnElMotor() {
		when(repository.hasStudies(1)).thenReturn(true);
		when(repository.hasStudies(2)).thenReturn(false);
		assertTrue(adapter.hasStudies(1));
		assertFalse(adapter.hasStudies(2));
	}
}
