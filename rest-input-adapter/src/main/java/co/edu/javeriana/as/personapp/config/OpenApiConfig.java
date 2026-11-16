package co.edu.javeriana.as.personapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Ficha de la API publicada en {@code /api-docs} y en Swagger UI (HU-028). Los
 * contratos vivos están en la wiki del repositorio, así que la documentación
 * generada apunta allí en lugar de repetirlos.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI personappOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("PersonAPP API")
						.version("v1")
						.description("""
								CRUD de Persona, Profesión, Teléfono y Estudios sobre MariaDB o MongoDB. \
								Cada operación recibe el motor en el parámetro de consulta `database`, \
								con los valores `MARIA` o `MONGO`, y no se usa ninguno por defecto. \
								Las seis operaciones por recurso son tres lecturas —consultar por \
								identificador, listar todos y contar— y tres escrituras —crear, editar y \
								eliminar—; el listado parcial paginado es una operación adicional. \
								Los errores usan el cuerpo común `{ "error": "ERR-xx", "message": "..." }`.""")
						.license(new License().name("Apache License 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0")))
				.externalDocs(new ExternalDocumentation()
						.description("Contrato de la API REST en la wiki del equipo")
						.url("https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/blob/main/docs/wiki/03-contratos/api-rest.md"));
	}
}
