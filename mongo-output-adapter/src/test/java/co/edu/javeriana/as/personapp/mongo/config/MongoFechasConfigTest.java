package co.edu.javeriana.as.personapp.mongo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;

import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.MongoClientSettings;

import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;

/**
 * HU-023: la fecha de un estudio no depende de la zona horaria de la JVM. Se prueba
 * en America/Bogota (UTC−5), donde la conversión por defecto corre el día.
 */
class MongoFechasConfigTest {

	private static final LocalDate GRADO = LocalDate.of(2015, 12, 1);
	private static final Instant MEDIANOCHE_UTC = Instant.parse("2015-12-01T00:00:00Z");

	private TimeZone zonaOriginal;

	@BeforeEach
	void usarZonaDeBogota() {
		zonaOriginal = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

	@AfterEach
	void restaurarZona() {
		TimeZone.setDefault(zonaOriginal);
	}

	private static MappingMongoConverter conversor(MongoCustomConversions conversions) {
		MongoMappingContext context = new MongoMappingContext();
		context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		context.afterPropertiesSet();
		MappingMongoConverter converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
		converter.setCustomConversions(conversions);
		converter.afterPropertiesSet();
		return converter;
	}

	private static final Supplier<MappingMongoConverter> CONFIGURADO = () -> conversor(
			new MongoFechasConfig().mongoCustomConversions());

	@Test
	void guardaLaFechaALaMedianocheUtc() {
		EstudiosDocument estudio = new EstudiosDocument("1-2", null, null, GRADO, null);
		Document guardado = new Document();
		CONFIGURADO.get().write(estudio, guardado);

		BsonDocument bson = guardado.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
		assertEquals(MEDIANOCHE_UTC.toEpochMilli(), bson.getDateTime("fecha").getValue());
	}

	@Test
	void leeElMismoDiaQueSeGuardo() {
		Document medianoche = new Document("_id", "1-2").append("fecha", Date.from(MEDIANOCHE_UTC));
		assertEquals(GRADO, CONFIGURADO.get().read(EstudiosDocument.class, medianoche).getFecha());

		// Los DML de MongoDB guardan las fechas a las 12:00 UTC (#47).
		Document mediodia = new Document("_id", "1-2").append("fecha", Date.from(Instant.parse("2015-12-01T12:00:00Z")));
		assertEquals(GRADO, CONFIGURADO.get().read(EstudiosDocument.class, mediodia).getFecha());
	}

	@Test
	void sinLaConfiguracionLaLecturaCorreElDia() {
		Document medianoche = new Document("_id", "1-2").append("fecha", Date.from(MEDIANOCHE_UTC));
		LocalDate leida = conversor(new MongoCustomConversions(List.of())).read(EstudiosDocument.class, medianoche)
				.getFecha();
		assertEquals(GRADO.minusDays(1), leida, "el defecto de #47 que corrige HU-023");
	}
}
