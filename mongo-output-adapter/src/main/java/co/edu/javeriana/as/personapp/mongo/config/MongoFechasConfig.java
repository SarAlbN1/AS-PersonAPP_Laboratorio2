package co.edu.javeriana.as.personapp.mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * HU-023: las fechas de Estudios (LocalDate) se guardan y se leen en UTC.
 * Sin esto, Spring Data MongoDB las convierte con la zona horaria de la JVM y una
 * misma fecha queda distinta entre una máquina local y un contenedor en UTC (#47).
 * Con los códecs del driver, 2015-12-01 se guarda como 2015-12-01T00:00Z y se lee
 * igual en cualquier zona.
 */
@Configuration
public class MongoFechasConfig {

	@Bean
	public MongoCustomConversions mongoCustomConversions() {
		return MongoCustomConversions.create(adapter -> adapter.useNativeDriverJavaTimeCodecs());
	}
}
