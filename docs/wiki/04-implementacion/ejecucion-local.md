# Ejecución local: plan de reproducción

> **Estado:** HU-003 preparó y verificó el ambiente JDK 11, Maven y ambos motores. También se ejecutaron los scripts y el build (ver [ambiente local verificado](#ambiente-local-verificado-hu-003)). HU-005 arrancó REST y CLI de la plantilla con JDK 11 (ver [arranque](#arranque-de-rest-y-cli-hu-005)). La entrega se verifica en JDK 11. HU-004 verificó la carga inicial en ambos motores y alineó el DML de MongoDB con la base `persona_db`. El DDL de MariaDB elimina el esquema y el usuario antes de recrearlos.

## Fase 1: línea base en JDK 11

1. Clonar el [repositorio del equipo](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2). El repositorio del docente solo fue la plantilla inicial.
2. Instalar JDK 11, Maven y los servidores MariaDB y MongoDB requeridos.
3. Registrar `java -version`, `mvn -version` y versión de ambos motores.
4. Inspeccionar y ejecutar DDL/DML, verificando el nombre de la base MongoDB.
5. Ejecutar `mvn clean install` desde la raíz multimódulo.
6. Arrancar y probar por separado `PersonAppRestApi` y `PersonAppCli`.
7. Revisar `http://localhost:3000/swagger-ui.html`, que redirige a `/swagger-ui/index.html`.
8. Reproducir, diagnosticar y corregir el error de Mapper indicado por el profesor.

## Ambiente local verificado (HU-003)

Procedimiento ejecutado el 2026-11-08 desde un clon limpio en Windows 11 (commit `9d44066`). Las versiones obtenidas están en [versiones](versiones.md#ambiente-de-línea-base-verificado-hu-003).

**Requisitos:** Git, Docker Desktop, JDK 11 (Temurin) y Maven 3.9.x. Maven se instala desde el zip oficial de Apache y se verifica con su SHA-512.

1. Clonar el repositorio del equipo y entrar en la carpeta.
2. Activar JDK 11 y Maven en la terminal. Si el JDK por defecto del equipo es otro, basta con fijarlos para esa sesión, sin cambiar la configuración global:

   ```powershell
   $env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-11.<versión>-hotspot'
   $env:Path = "$env:JAVA_HOME\bin;<ruta-de-maven>\bin;$env:Path"
   java -version; mvn -version
   ```

3. Levantar los motores en los puertos que esperan los `application.properties`. La contraseña de root es local: elegir un valor propio, guardarlo en una variable de la terminal y no subirlo al repositorio. En PowerShell:

   ```powershell
   $env:PERSONAPP_ROOT_PW = 'cambia-esta-clave'
   docker run -d --name personapp-mariadb -p 3307:3306 -e "MARIADB_ROOT_PASSWORD=$env:PERSONAPP_ROOT_PW" -v personapp-mariadb-data:/var/lib/mysql mariadb:11.4
   docker run -d --name personapp-mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e "MONGO_INITDB_ROOT_PASSWORD=$env:PERSONAPP_ROOT_PW" -v personapp-mongo-data:/data/db mongo:6.0
   ```

4. Ejecutar los scripts desde `scripts/` en Git Bash. La variable de PowerShell no pasa a otra terminal, así que hay que declararla de nuevo con el mismo valor. El DDL de MariaDB borra y recrea el esquema `persona_db` y su usuario.

   ```bash
   export PERSONAPP_ROOT_PW='cambia-esta-clave'
   docker exec -i personapp-mariadb mariadb -uroot -p"$PERSONAPP_ROOT_PW" < persona_ddl_maria.sql
   docker exec -i personapp-mariadb mariadb -uroot -p"$PERSONAPP_ROOT_PW" < persona_dml_maria.sql
   docker exec -i personapp-mongo mongosh --quiet -u root -p "$PERSONAPP_ROOT_PW" --authenticationDatabase admin < persona_ddl_mongo.js
   docker exec -i personapp-mongo mongosh --quiet -u root -p "$PERSONAPP_ROOT_PW" --authenticationDatabase admin < persona_dml_mongo.js
   ```

5. Comprobar el acceso con el usuario de la aplicación (`persona_db`, definido en los scripts y en los `application.properties`):

   ```bash
   docker exec personapp-mariadb mariadb -upersona_db -ppersona_db -e "SHOW TABLES FROM persona_db; SELECT COUNT(*) FROM persona_db.persona;"
   docker exec personapp-mongo mongosh --quiet -u persona_db -p persona_db --authenticationDatabase admin persona_db --eval "db.persona.countDocuments()"
   ```

6. Compilar desde la raíz: `mvn -B clean install -DskipTests`.

**Resultados observados:**
- MariaDB: las cuatro tablas creadas y cinco personas; el usuario `persona_db` accede.
- MongoDB: en la línea base de HU-003, el usuario `persona_db` se creó en `admin`. El DML original cargaba cinco personas en `prueba_db`. Para comprobar el acceso a `persona_db`, se ajustó el nombre de la base en una copia temporal del script. La HU-004 corrigió el script del repositorio.
- Build: `BUILD SUCCESS` en los ocho artefactos.

Para reiniciar los motores después de apagar el equipo: `docker start personapp-mariadb personapp-mongo`.

## Verificación de datos iniciales (HU-004)

Sigue los pasos de carga y comprobación de la sección anterior en instancias de prueba limpias. El DML actual de MongoDB carga en `persona_db.persona`, la misma base que usan REST y CLI.

Para comprobar también los registros, ejecuta `SELECT * FROM persona_db.persona ORDER BY cc` en MariaDB y `db.getSiblingDB("persona_db").persona.find().sort({_id: 1}).toArray()` en MongoDB.

El 9 de noviembre de 2026 se ejecutaron los cuatro scripts de la rama HU-004 en contenedores limpios con las imágenes `mariadb:11.4` y `mongo:6.0`. Los scripts de MongoDB se ejecutaron con `mongosh --file`. Las consultas de tablas, colección y registros se hicieron con la cuenta de aplicación `persona_db`.

| Motor | Versión observada | Estructuras y registros |
|---|---|---|
| MariaDB | 11.4.13 | `SHOW TABLES FROM persona_db` devolvió `estudios`, `persona`, `profesion` y `telefono`. `SELECT COUNT(*) FROM persona_db.persona` devolvió 5. |
| MongoDB | 6.0.28 | `getCollectionNames()` devolvió `["persona"]` en `persona_db` y `[]` en `prueba_db`. `db.persona.countDocuments()` devolvió 5. |

Los identificadores observados en ambos motores fueron 123456789, 147258369, 321654987, 963852741 y 987654321. La persona 123456789 es Pepe Perez, de 30 años. Para 987654321, MariaDB devolvió `NULL` en `edad` y MongoDB devolvió el documento sin ese campo.

## Arranque de REST y CLI (HU-005)

Procedimiento ejecutado el 2026-11-09 en Windows 11 sobre el commit `f727fcd`, con los motores cargados según las dos secciones anteriores. JDK: Temurin 11.0.33 (zip portable, fijado solo para la terminal). Maven: 3.9.14.

**Por qué no se usan `java -jar` ni `spring-boot:run`.** Los JAR de `rest-input-adapter` y `cli-input-adapter` pesan unos 12 KB y no son ejecutables, porque el `spring-boot-maven-plugin` no declara el objetivo `repackage`. `spring-boot:run` dentro de un módulo falla porque el POM usa `${revision}` sin `flatten-maven-plugin`, y los POM instalados conservan ese literal. Sin cambiar el código, las aplicaciones se arrancan con el classpath que calcula el reactor.

1. Desde la raíz, con JDK 11 activo en la terminal, compilar y calcular el classpath de cada entrada:

   ```powershell
   mvn -B clean install -DskipTests
   mvn -B -q -pl rest-input-adapter,cli-input-adapter -am dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
   ```

2. Arrancar REST (puerto 3000):

   ```powershell
   java -cp "rest-input-adapter\target\classes;$(Get-Content rest-input-adapter\target\classpath.txt)" co.edu.javeriana.as.personapp.PersonAppRestApi
   ```

3. En otra terminal, con JDK 11 activo, arrancar la CLI:

   ```powershell
   java -cp "cli-input-adapter\target\classes;$(Get-Content cli-input-adapter\target\classpath.txt)" co.edu.javeriana.as.personapp.PersonAppCli
   ```

**Resultados observados en REST.** Log: `Starting PersonAppRestApi using Java 11.0.33`, `Tomcat started on port(s): 3000 (http)` y `Started PersonAppRestApi in 5.38 seconds`.

| Solicitud | HTTP | Resultado |
|---|---|---|
| `GET /api/v1/persona/MARIA` | 200 | Cinco personas con `"database":"MariaDB"` y `"status":"OK"`, ordenadas por `dni`. |
| `GET /api/v1/persona/MONGO` | 200 | Las mismas cinco personas con `"database":"MongoDB"`, en orden de inserción (123456789, 987654321, 321654987, 147258369, 963852741). Para 987654321 la edad llega como la cadena `"null"`. |
| `GET /api/v1/persona/OTRO` | 200 | `[]`. El adaptador registra `Invalid database option` y no devuelve error. |
| `POST /api/v1/persona` con `dni`, `firstName`, `lastName`, `age`, `sex` y `"database":"MARIA"` | 500 | `java.lang.NullPointerException: gender is marked non-null but is null` en `PersonaMapperMaria.validateGenero` (`PersonaMapperMaria.java:40`), llamado desde `fromDomainToAdapter` (línea 33). No se insertó ningún registro: MariaDB sigue con cinco personas. El diagnóstico corresponde a HU-006. |
| `GET /swagger-ui.html` | 200 | Redirige a `/swagger-ui/index.html`. |
| `GET /api-docs` | 200 | OpenAPI `3.0.1` con las rutas `/api/v1/persona` y `/api/v1/persona/{database}`. |

**Resultados observados en CLI.** Log: `Starting PersonAppCli using Java 11.0.33` y `Started PersonAppCli in 3.824 seconds`. Entrada usada: `1` (Persona), `1` (MariaDB), `1` (ver todo), `0`, `2` (MongoDB), `1`, `0`, `0`, `2` (Profesión), `0` (salir).

- En una sola sesión se listaron las cinco personas de MariaDB y luego las cinco de MongoDB, sin reiniciar. Formato: `PersonaModelCli(cc=123456789, nombre=Pepe, apellido=Perez, genero=MALE, edad=30)`.
- La opción 2 del menú principal (Profesión) registra `WARN ... MenuPrincipal : Implementar Menu`.
- La opción 0 termina la aplicación con código de salida 0.

## Corrección del Mapper REST (HU-006)

Procedimiento ejecutado el 2026-11-10 sobre `e1a360d`, con el ambiente y el arranque de las secciones anteriores.

**Reproducción** (2026-11-09, antes de la corrección). Entradas de `POST /api/v1/persona`:
- `{"dni":"999","firstName":"Prueba","lastName":"Mapper","age":"30","sex":"M","database":"MARIA"}`
- `{"dni":"998","firstName":"Prueba","lastName":"Mapper","age":"30","sex":"F","database":"MONGO"}`

Salida: `500` en ambos motores y ningún registro guardado. Error: `java.lang.NullPointerException: gender is marked non-null but is null`, con esta cadena de llamadas:

```text
PersonaControllerV1.crearPersona:40 → PersonaInputAdapterRest.crearPersona:72 → PersonUseCase.create:35
→ PersonOutputAdapter{Maria|Mongo}.save → PersonaMapper{Maria|Mongo}.fromDomainToAdapter:33 → validateGenero:40
```

**Causa.** `PersonaMapperRest.fromAdapterToDomain` era un método sin implementar y devolvía `new Person()`. Todos los campos llegaban nulos a los mappers de salida, y `validateGenero` es el primero que exige un valor. Los mappers de MariaDB y MongoDB no tienen el defecto.

**Corrección.** Solo cambia `PersonaMapperRest.fromAdapterToDomain`:
- `dni` pasa a entero; si falta o no es numérico, se lanza `IllegalArgumentException`.
- `firstName` y `lastName` se copian sin cambios.
- `age` pasa a entero, o queda nula si viene vacía.
- `sex` admite `M`/`MALE` y `F`/`FEMALE` sin distinguir mayúsculas. Cualquier otro valor se rechaza, según la decisión D-01 de HU-011.

Las respuestas HTTP para entradas inválidas (hoy `500`) corresponden a la validación de Persona (HU-014).

**Prueba de regresión.** `rest-input-adapter/src/test/.../PersonaMapperRestTest.java`, con JUnit 5.8.2 como dependencia de prueba. Sus seis casos cubren la transformación completa, los dos formatos de sexo, la edad vacía, el rechazo de sexo y `dni` inválidos, y la respuesta construida a partir del dominio.

```powershell
mvn -B clean install
```

`Tests run: 6, Failures: 0, Errors: 0` y `BUILD SUCCESS` en los ocho artefactos. Con el método original, la misma prueba falla en cinco de los seis casos. Para ejecutar solo este módulo se usa `mvn -B -pl rest-input-adapter -am test`, por el `${revision}` descrito en HU-005.

**Resultados en REST** (arranque como en HU-005). Los cuerpos enviados a `POST /api/v1/persona` son los que muestra la tabla. Los que no aparecen son `firstName` `Prueba` y `lastName` `Mapper` en las dos primeras filas, `X`/`Y` en la de sexo inválido, y `Sin`/`Edad` en las de edad ausente.

| Solicitud | HTTP | Resultado |
|---|---|---|
| `{"dni":"999","age":"30","sex":"M","database":"MARIA"}` (2026-11-10) | 200 | Responde la persona con `"sex":"MALE"`. MariaDB guarda `999, Prueba, Mapper, M, 30`. |
| `{"dni":"998","age":"25","sex":"F","database":"MONGO"}` (2026-11-10) | 200 | MongoDB guarda `{_id: 998, genero: 'F', edad: 25}`. La respuesta indica `"database":"MariaDB"` porque `PersonaInputAdapterRest.crearPersona` usa siempre la etiqueta de MariaDB; queda para HU-014. |
| `{"dni":"997","age":"1","sex":"O","database":"MARIA"}` (2026-11-10) | 500 | `IllegalArgumentException: Sexo no admitido: O. Use M o F.` No se guarda nada. |
| `{"dni":"996","sex":"F","database":"MARIA"}`, sin `age` (2026-11-11) | 200 | MariaDB guarda `edad` como `NULL`. |
| `{"dni":"995","age":"","sex":"M","database":"MONGO"}` (2026-11-11) | 200 | MongoDB guarda el documento sin el campo `edad`, igual que la persona 987654321 del DML. `isValidAge` no se invoca en este flujo. |
| `GET /api/v1/persona/MARIA` y `/MONGO` | 200 | Cada motor incluye la persona creada en él, además de las cinco iniciales. Una edad ausente se muestra como `"age":"null"`, igual que en HU-005. |

Al terminar se borraron los registros de prueba, y ambos motores volvieron a cinco personas.

## Ejecución con Docker Compose (HU-034)

Con Docker Compose, la aplicación y las dos bases se construyen y ejecutan en contenedores, sin instalar JDK ni Maven. Hay dos imágenes y dos bases:

| Servicio | Imagen | Qué ejecuta |
|---|---|---|
| `rest` | `personapp-rest:local`, construida desde el `Dockerfile` | `rest-input-adapter-*-exec.jar` en `eclipse-temurin:25-jre`, publicado en el puerto 3000 |
| `cli` | `personapp-cli:local`, construida desde el `Dockerfile` | `cli-input-adapter-*-exec.jar` en `eclipse-temurin:25-jre`, interactiva y con perfil `cli` |
| `mariadb` | `mariadb:11.4` | Servidor con el volumen `mariadb-data` |
| `mongo` | `mongo:6.0` | Servidor con el volumen `mongo-data` |

El `Dockerfile` construye una sola vez el reactor de siete módulos con `./mvnw -B -ntp clean package` en `eclipse-temurin:25-jdk`, pruebas incluidas, y copia cada `-exec.jar` a su imagen. Los módulos Maven no se convierten en servicios: cada imagen empaqueta una entrada, y las dos usan los mismos adaptadores de salida. Las imágenes corren con el usuario `personapp`, sin privilegios.

**Requisitos:** Git y Docker con Compose v2. El `.gitattributes` de HU-007 garantiza que `mvnw` llegue con finales de línea LF, también en Windows.

### Configuración

La configuración sale de `.env`, que no se versiona. La plantilla es `.env.example`:

| Variable | Uso | Valor por defecto |
|---|---|---|
| `PERSONAPP_ROOT_PW` | Clave de root de MariaDB y MongoDB. Solo la usan los contenedores de las bases. | Obligatoria |
| `PERSONAPP_DB_USER` / `PERSONAPP_DB_PASSWORD` | Usuario de la aplicación. Debe coincidir con el que crean los DDL. | `persona_db` / `persona_db` |
| `PERSONAPP_REST_PORT` | Puerto publicado de REST | `3000` |
| `PERSONAPP_TZ` | Zona horaria de la JVM (`-Duser.timezone`) | `America/Bogota` |

REST y CLI reciben la conexión por entorno (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` y `SPRING_MONGODB_URI`), que reemplaza los valores de `localhost` de los `application.properties`. Dentro de la red de Compose, las bases se alcanzan como `mariadb:3306` y `mongo:27017`, y sus puertos no se publican en la máquina.

La zona horaria se fija porque Spring Data MongoDB lee y guarda las fechas con la zona de la JVM, y una imagen en UTC registraría otra fecha que una ejecución local en Bogotá. La corrección de fondo corresponde a HU-023.

El usuario `persona_db` y su clave vienen fijos en los scripts DDL de la plantilla. Para cambiarlos hay que modificar esos scripts.

### Carga de datos

Las bases montan los cuatro scripts de `scripts/` en `/docker-entrypoint-initdb.d`, y cada imagen los ejecuta en orden (`01-` DDL, `02-` DML) **solo cuando su volumen está vacío**. Un reinicio o una recreación de los contenedores no borra datos ni vuelve a cargar los scripts. Para cargar de nuevo los datos iniciales hay que borrar los volúmenes (`docker compose down -v`). Como los scripts se montan y no se copian, los cambios de `scripts/` se aplican en la siguiente carga sin reconstruir nada.

### Procedimiento

```bash
cp .env.example .env                 # y cambiar PERSONAPP_ROOT_PW
docker compose up -d --build         # construye las imágenes y levanta mariadb, mongo y rest
docker compose ps                    # las bases y rest deben quedar healthy
curl http://localhost:3000/api/v1/persona/MARIA
curl http://localhost:3000/api/v1/persona/MONGO
docker compose run --rm cli          # CLI interactiva contra las mismas bases
docker compose logs -f rest          # registros de REST
docker compose down                  # detiene y elimina los contenedores; conserva los volúmenes
docker compose down -v               # además borra los volúmenes y los datos
```

Swagger UI queda en `http://localhost:3000/swagger-ui.html`. REST espera a que las dos bases pasen su comprobación de salud: `healthcheck.sh` en MariaDB y `ping` en MongoDB. La comprobación de REST verifica que el puerto 3000 acepte conexiones.

### Resultados

El 12 de noviembre de 2026 se ejecutó el procedimiento en Windows 11 con Docker Compose v5.5.1, en la rama `feat/HU-034-contenedores`, sobre `main` con HU-007 (`723b6b7`, mismo árbol que el último commit revisado del #46, `866e165`). Se trabajó sobre un clon limpio con `core.autocrlf=true`, que gracias al `.gitattributes` trae `mvnw` en LF, y las imágenes se construyeron con `--no-cache`:

| Prueba | Resultado |
|---|---|
| `docker compose --profile cli build --no-cache` | `./mvnw` construye el reactor con `Tests run: 6, Failures: 0` y `BUILD SUCCESS`, y se generan las dos imágenes |
| `docker compose up -d` | MariaDB y MongoDB quedan `healthy` y cargan los DDL/DML, y REST queda `healthy` |
| `GET /api/v1/persona/MARIA` y `/MONGO` | 200 con las cinco personas de cada motor |
| `/swagger-ui.html` y `/api-docs` | 200; OpenAPI 3.1.0 |
| `POST` con `{"dni":"989","age":"31","sex":"F","database":"MARIA"}` y `{"dni":"988","age":"32","sex":"M","database":"MONGO"}` | 200. Cada persona queda solo en su motor |
| `docker compose run --rm cli`: Persona → MariaDB → ver todo, y luego MongoDB → ver todo | En una sola sesión lista seis personas de MariaDB (con la 989) y seis de MongoDB (con la 988). Termina con código 0 |
| `docker compose down` y luego `up -d --force-recreate` | Los volúmenes se conservan. Siguen seis personas por motor y los scripts no se vuelven a ejecutar |
| Zona horaria de REST | La JVM informa `Picked up JAVA_TOOL_OPTIONS: -Duser.timezone=America/Bogota` |
| `docker compose down -v` | Elimina contenedores, red y los dos volúmenes |

Antes del `.gitattributes`, un checkout de Windows con `core.autocrlf=true` traía `mvnw` con CRLF, y `./mvnw` fallaba en el contenedor con `not found`.

**Pendiente:** repetir la carga y las lecturas con los DML que agregan teléfonos y estudios (#47).

## Fase 2: funcionalidad faltante

Completar CRUD y exposición REST/CLI para Persona, Profesión, Estudios y Teléfono en MariaDB y MongoDB.
