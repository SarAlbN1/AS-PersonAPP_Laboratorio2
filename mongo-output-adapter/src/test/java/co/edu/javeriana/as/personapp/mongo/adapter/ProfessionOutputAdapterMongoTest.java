package co.edu.javeriana.as.personapp.mongo.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument;
import co.edu.javeriana.as.personapp.mongo.mapper.ProfesionMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.ProfesionRepositoryMongo;

/** HU-017: orden, página y conteo pedidos a MongoDB y errores de escritura propagados. */
@ExtendWith(MockitoExtension.class)
class ProfessionOutputAdapterMongoTest {

	@Mock
	private ProfesionRepositoryMongo repository;

	@Mock
	private MongoTemplate mongoTemplate;

	@Spy
	private ProfesionMapperMongo mapper;

	@InjectMocks
	private ProfessionOutputAdapterMongo adapter;

	private static ProfesionDocument document(int id, String nom) {
		return new ProfesionDocument(id, nom, "d" + id, null);
	}

	@Test
	void listarTodosPideOrdenPorIdAlMotor() {
		when(repository.findAll(any(Sort.class))).thenReturn(List.of(document(1, "A"), document(2, "B")));
		List<Profession> result = adapter.find();
		ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
		verify(repository).findAll(sort.capture());
		assertEquals(Sort.by(Sort.Direction.ASC, "_id"), sort.getValue());
		assertEquals(List.of(1, 2), result.stream().map(Profession::getIdentification).toList());
		assertNull(result.get(0).getStudies(), "C-10: no se cargan los estudios");
	}

	@Test
	void listarParcialPasaPaginaTamanoYOrden() {
		when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
		assertTrue(adapter.find(4, 100).isEmpty());
		ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(repository).findAll(pageable.capture());
		assertEquals(4, pageable.getValue().getPageNumber());
		assertEquals(100, pageable.getValue().getPageSize());
		assertEquals(Sort.by(Sort.Direction.ASC, "_id"), pageable.getValue().getSort());
	}

	@Test
	void consultarInexistenteDevuelveNull() {
		when(repository.findById(9)).thenReturn(Optional.empty());
		assertNull(adapter.findById(9));
	}

	@Test
	void contarUsaCountDelMotor() {
		when(repository.count()).thenReturn(3L);
		assertEquals(3, adapter.count());
	}

	@Test
	void eliminarConfirmaQueYaNoExiste() {
		when(repository.existsById(1)).thenReturn(false);
		assertTrue(adapter.delete(1));
		verify(repository).deleteById(1);
	}

	@Test
	void errorDeEscrituraSePropaga() {
		when(repository.save(any(ProfesionDocument.class))).thenThrow(new DuplicateKeyException("E11000"));
		assertThrows(DuplicateKeyException.class, () -> adapter.save(new Profession(1, "A", "d", null)));
	}

	@Test
	void dependientesSeBuscanPorPrimaryProfesion() {
		when(mongoTemplate.exists(any(Query.class), eq(EstudiosDocument.class))).thenReturn(true);
		assertTrue(adapter.hasStudies(7));
		ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
		verify(mongoTemplate).exists(query.capture(), eq(EstudiosDocument.class));
		assertEquals(new Document("primaryProfesion", 7), query.getValue().getQueryObject());
	}
}
