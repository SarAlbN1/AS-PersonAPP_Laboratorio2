package co.edu.javeriana.as.personapp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.model.request.EstudioRequest;
import co.edu.javeriana.as.personapp.model.response.EstudioResponse;

/** HU-024: el mapper lleva las dos partes de la clave y no falla con campos ausentes. */
class EstudioMapperRestTest {

	private final EstudioMapperRest mapper = new EstudioMapperRest();

	@Test
	void copiaLaClaveCompuestaLaFechaYLaUniversidad() {
		Study study = mapper.fromAdapterToDomain(
				new EstudioRequest(1, 123456789, LocalDate.of(2015, 12, 1), "Javeriana"));
		assertEquals(1, study.getProfession().getIdentification());
		assertEquals(123456789, study.getPerson().getIdentification());
		assertEquals(LocalDate.of(2015, 12, 1), study.getGraduationDate());
		assertEquals("Javeriana", study.getUniversityName());
	}

	@Test
	void unCampoAusenteQuedaSinAsignarParaQueElCasoDeUsoDeErr01() {
		Study study = mapper.fromAdapterToDomain(new EstudioRequest(null, null, null, null));
		assertNull(study.getProfession());
		assertNull(study.getPerson());
		assertNull(study.getGraduationDate());
		assertNull(study.getUniversityName());
	}

	@Test
	void laRespuestaLlevaSoloLasClavesDeLasReferencias() {
		Profession profession = new Profession();
		profession.setIdentification(2);
		profession.setName("Medicina");
		Person person = new Person();
		person.setIdentification(321654987);
		person.setFirstName("Pepa");
		Study study = new Study(person, profession, LocalDate.of(2018, 6, 30), null);
		assertEquals(new EstudioResponse(2, 321654987, LocalDate.of(2018, 6, 30), null),
				mapper.fromDomainToAdapterRest(study));
	}
}
