# PersonAPP · Laboratorio 2

Proyecto de Arquitectura de Software de la Pontificia Universidad Javeriana desarrollado por **Sara Albarracín, Miguel Sarmiento y Juan Diego Rojas**.

PersonAPP es un servicio web con arquitectura hexagonal, casos de uso (Service) y repositorios (Repository) para gestionar Persona, Profesión, Teléfono y Estudios desde REST y CLI. El equipo acordó seis operaciones por recurso: crear, listar todos, listar parcial, editar, eliminar y contar.

**[Explorar la wiki de PersonAPP](docs/wiki/Home.md)**

**Stack de entrega.** JDK 11, Spring Boot 2.7.11, Maven, REST, CLI, MariaDB, MongoDB y Swagger 3.

**Estado observado.** Los cuatro modelos de dominio existen. Persona tiene puertos, caso de uso y persistencia para ambos motores. REST declara listado y alta, con el mapper de alta incompleto. CLI conecta el listado. El conteo de Persona existe en el caso de uso. La compilación, los arranques y la persistencia de la entrega siguen pendientes de comprobación.

## Explorar la wiki

Empieza por el [índice de la wiki](docs/wiki/Home.md). Desde allí puedes seguir el recorrido completo, desde los requisitos del laboratorio hasta la entrega.

| Si quieres entender… | Empieza aquí |
|---|---|
| Qué pide el laboratorio y qué alcance acordó el equipo | [Producto y requisitos](docs/wiki/01-producto/README.md) |
| Cómo se conectan el dominio, REST, CLI y los motores | [Arquitectura](docs/wiki/02-arquitectura/README.md) |
| Qué operaciones, entradas y errores se esperan | [Contratos](docs/wiki/03-contratos/README.md) |
| Cómo está organizado el código y cómo probarlo | [Implementación y pruebas](docs/wiki/04-implementacion/README.md) |
| Cómo colaborar y preparar la entrega | [Proceso](docs/wiki/05-proceso/README.md) y [CONTRIBUTING](CONTRIBUTING.md) |
| Qué trabajo sigue y qué lo desbloquea | [Backlog](docs/wiki/06-backlog/README.md) |

## Preparación y ejecución

1. Instalar JDK 11 y Maven. Configurar Lombok en el IDE y preparar MariaDB en el puerto 3307 y MongoDB en el 27017.
2. Revisar los scripts DDL/DML en `scripts/` y los `application.properties` de cada entrada. El DDL de MariaDB elimina el esquema y el usuario antes de recrearlos. Usar bases de prueba. El DML Mongo selecciona `prueba_db` y las aplicaciones consultan `persona_db`. Alinear la carga y la conexión del entorno de pruebas y registrar el ajuste.
3. Desde la raíz, el comando de compilación previsto es `mvn clean install`.
4. Iniciar por separado `PersonAppRestApi` y `PersonAppCli`. REST está configurado en el puerto 3000. OpenAPI se configura en `/api-docs` y la dirección prevista de Swagger UI es `http://localhost:3000/swagger-ui.html`.

Los comandos exactos de arranque y la reproducción desde un clon limpio están pendientes de verificación. [Ejecución local](docs/wiki/04-implementacion/ejecucion-local.md).

## Informe y asistentes

- [Informe académico](docs/informe/main.tex) y [lista de entrega](docs/wiki/05-proceso/entrega.md).
- [CLAUDE.md](CLAUDE.md) para instrucciones de trabajo con asistentes.


## Referencias

- [Repositorio docente de origen](https://github.com/andres-karoll/personapp-hexa-spring-boot), del que parte el fork del equipo.
- [Decisiones de arquitectura](docs/wiki/02-arquitectura/decisiones-adr.md) y [versiones de la entrega](docs/wiki/04-implementacion/versiones.md).
