# PersonAPP · Laboratorio 2

Proyecto de Arquitectura de Software de la Pontificia Universidad Javeriana desarrollado por **Sara Albarracín, Miguel Sarmiento y Juan Diego Rojas**.

PersonAPP es un servicio web con arquitectura hexagonal, casos de uso (Service) y repositorios (Repository) para gestionar Persona, Profesión, Teléfono y Estudios desde REST y CLI. El equipo acordó seis operaciones por recurso: tres lecturas (consultar por identificador, listar todos y contar) y tres escrituras (crear, editar y eliminar). El listado parcial es una operación adicional.

**[Explorar la wiki de PersonAPP](docs/wiki/Home.md)**

**Estado.** Los cuatro recursos tienen las seis operaciones y el listado parcial en REST y CLI, con MariaDB y MongoDB, y la selección del motor es por petición. HU-033 agrega la consulta de una persona con todos sus teléfonos. El avance por historia está en el [backlog](docs/wiki/06-backlog/README.md).

## Repositorios y colaboración

El [repositorio del docente](https://github.com/andres-karoll/personapp-hexa-spring-boot) se usó únicamente como plantilla inicial. El equipo no sincronizará su repositorio con la plantilla ni enviará commits, abrirá pull requests o solicitará cambios en el repositorio del docente.

Todo el trabajo del laboratorio, incluidas las ramas, issues y revisiones, se realiza exclusivamente en el [repositorio del equipo](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2). Clona ese repositorio para contribuir.

Crea tu rama desde `main` con el formato `<tipo>/HU-###-descripcion` y abre el PR hacia `main` del fork. Incluye la evidencia de verificación y solicita la revisión de otro integrante.

## Explorar la wiki

Empieza por el [índice de la wiki](docs/wiki/Home.md). Desde allí puedes seguir el recorrido completo, desde los requisitos del laboratorio hasta la entrega.

| Si quieres entender… | Empieza aquí |
|---|---|
| Qué pide el laboratorio y qué alcance acordó el equipo | [Producto y requisitos](docs/wiki/01-producto/README.md) |
| Cómo se conectan el dominio, REST, CLI y los motores | [Arquitectura](docs/wiki/02-arquitectura/README.md) |
| Qué operaciones, entradas y errores se esperan | [Contratos](docs/wiki/03-contratos/README.md) |
| Cómo está organizado el código y cómo probarlo | [Implementación y pruebas](docs/wiki/04-implementacion/README.md) |
| Cómo colaborar y preparar la entrega | [Proceso](docs/wiki/05-proceso/README.md) |
| Qué trabajo sigue y qué lo desbloquea | [Backlog](docs/wiki/06-backlog/README.md) |

## Requisitos y versiones

| Componente | Versión verificada |
|---|---|
| Java | 25 (Eclipse Temurin) |
| Spring Boot | 4.1.2 |
| Maven | 3.10.0, incluido con el Wrapper (`./mvnw`) |
| MariaDB | 11.4 |
| MongoDB | 6.0 |
| Springdoc OpenAPI | 3.2.0 |

El detalle y la migración desde la línea base (JDK 11 y Spring Boot 2.7.11) están en [versiones](docs/wiki/04-implementacion/versiones.md).

## Preparación y ejecución

### Con Docker Compose (recomendado)

Requiere Git y Docker con Compose v2. No hace falta instalar JDK ni Maven: la imagen compila el proyecto con el Wrapper.

```bash
git clone https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2.git
cd AS-PersonAPP_Laboratorio2
cp .env.example .env              # cambiar PERSONAPP_ROOT_PW antes de seguir
docker compose up -d --build      # MariaDB, MongoDB y REST en http://localhost:3000
docker compose run --rm --build cli   # CLI interactiva, contra las mismas bases
docker compose down               # detiene y conserva los datos; down -v los borra
```

- **Scripts DDL y DML.** Las bases ejecutan los de `scripts/` solo la primera vez, cuando sus volúmenes están vacíos. Para volver a los datos iniciales: `docker compose down -v` y `docker compose up -d`.
- **`--build` en la CLI.** La CLI está en el perfil `cli`, así que `up --build` no la reconstruye. Sin `--build`, `run` usa la imagen `personapp-cli:local` que ya exista, aunque sea de una versión anterior del código.
- **Puertos.** Solo se publica REST (`PERSONAPP_REST_PORT`, 3000 por defecto). Las bases quedan dentro de la red de Compose.
- La configuración, los resultados y la limpieza están en [Ejecución con Docker Compose](docs/wiki/04-implementacion/ejecucion-local.md#ejecución-con-docker-compose-hu-034).

### Sin contenedores para la aplicación

1. Instalar JDK 25. Maven no hace falta porque se usa `./mvnw` (`mvnw.cmd` en Windows).
2. Tener MariaDB 11.4 en el puerto 3307 y MongoDB 6.0 en el 27017, que son los puertos de los `application.properties`. [Ambiente local](docs/wiki/04-implementacion/ejecucion-local.md#ambiente-local-verificado-hu-003) muestra cómo levantarlas con Docker.
3. Cargar los scripts con el usuario root, en este orden. **El DDL de MariaDB borra el esquema y el usuario `persona_db` antes de recrearlos**, así que conviene usar una base de prueba.

   ```bash
   mariadb -h 127.0.0.1 -P 3307 -uroot -p < scripts/persona_ddl_maria.sql
   mariadb -h 127.0.0.1 -P 3307 -uroot -p < scripts/persona_dml_maria.sql
   mongosh "mongodb://root:<clave>@127.0.0.1:27017/?authSource=admin" scripts/persona_ddl_mongo.js
   mongosh "mongodb://root:<clave>@127.0.0.1:27017/?authSource=admin" scripts/persona_dml_mongo.js
   ```

4. Compilar desde la raíz: `./mvnw -B clean install`.
5. Arrancar cada entrada en su propia consola:

   ```bash
   java -jar rest-input-adapter/target/rest-input-adapter-*-exec.jar   # REST en el puerto 3000
   java -jar cli-input-adapter/target/cli-input-adapter-*-exec.jar     # CLI interactiva
   ```

## Swagger y API REST

- Swagger UI: `http://localhost:3000/swagger-ui.html`.
- OpenAPI en JSON: `http://localhost:3000/api-docs`.

Todas las rutas exigen `database=MARIA` o `database=MONGO`, y cada petición elige su motor. Las rutas de los cuatro recursos (`persona`, `profesion`, `telefono` y `estudio`) son las mismas:

| Operación | Ruta |
|---|---|
| Consultar | `GET /api/v1/{recurso}/{clave}?database=` (en Estudios la clave es `/{idProfesion}/{ccPersona}`) |
| Listar todos | `GET /api/v1/{recurso}?database=` |
| Contar | `GET /api/v1/{recurso}/conteo?database=` |
| Crear | `POST /api/v1/{recurso}?database=` |
| Editar | `PUT /api/v1/{recurso}/{clave}?database=` |
| Eliminar | `DELETE /api/v1/{recurso}/{clave}?database=` |
| Listar parcial | `GET /api/v1/{recurso}/pagina?database=&page=0&size=10` |
| Persona con sus teléfonos | `GET /api/v1/persona/{cc}/telefonos?database=` |

Los errores responden `{"error":"ERR-xx","message":"…"}` con el [catálogo de errores](docs/wiki/03-contratos/validaciones-y-errores.md). Los cuerpos y las respuestas están en la [API REST](docs/wiki/03-contratos/api-rest.md).

## Comprobar REST y CLI

Con la aplicación arrancada y los datos iniciales cargados:

```bash
curl "http://localhost:3000/api/v1/persona/conteo?database=MARIA"          # 5
curl "http://localhost:3000/api/v1/persona/123456789/telefonos?database=MONGO"
curl -X POST "http://localhost:3000/api/v1/telefono?database=MARIA"      -H "Content-Type: application/json"      -d '{"number":"0012345","company":"Claro","ownerId":123456789}'      # 201
curl "http://localhost:3000/api/v1/telefono/0012345?database=MONGO"        # 404 ERR-03: los motores no se replican
curl -X DELETE "http://localhost:3000/api/v1/telefono/0012345?database=MARIA"   # 204
```

En la CLI, el menú principal ofrece 1 Persona, 2 Profesión, 3 Teléfono y 4 Estudios. Después se elige el motor (1 MariaDB, 2 MongoDB) y la operación (1 consultar, 2 listar, 3 contar, 4 crear, 5 editar, 6 eliminar, 7 listar parcial). Con 0 se vuelve al menú de motor y se puede cambiar de motor sin reiniciar. Los errores salen como `[ERR-xx] mensaje` y la sesión sigue.

Los guiones completos de cada recurso, con los resultados en los dos motores, están en [ejecución local](docs/wiki/04-implementacion/ejecucion-local.md) y en los PR de cada historia.

## Pruebas automáticas

```bash
./mvnw -B clean install                                    # pruebas unitarias
docker compose -f compose.yaml -f compose.pruebas.yaml up -d mariadb mongo
./mvnw -B clean install -Dmotores=arriba                   # agrega las pruebas contra los motores reales
docker compose -f compose.yaml -f compose.pruebas.yaml down -v
```

`compose.pruebas.yaml` publica las bases solo en `127.0.0.1`, en los puertos `PERSONAPP_MARIA_PORT` y `PERSONAPP_MONGO_PORT` (3307 y 27017 por defecto). Si esos puertos ya están ocupados, por ejemplo por las bases de [HU-003](docs/wiki/04-implementacion/ejecucion-local.md#ambiente-local-verificado-hu-003), hay que exportar otros en la misma consola **antes** de los dos comandos, porque Maven no lee el `.env`:

```bash
export PERSONAPP_MARIA_PORT=3407 PERSONAPP_MONGO_PORT=27117
```

Más detalle en [pruebas](docs/wiki/04-implementacion/pruebas.md).

## Informe y asistentes

- [Informe académico](docs/informe/main.tex) y [lista de entrega](docs/wiki/05-proceso/entrega.md).

## Referencias

- [Decisiones de arquitectura](docs/wiki/02-arquitectura/decisiones-adr.md) y [versiones de la entrega](docs/wiki/04-implementacion/versiones.md), que incluye el [procedimiento reproducible de la actualización tecnológica](docs/wiki/04-implementacion/versiones.md#migración-ejecutada-hu-007).
