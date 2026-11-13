# Versiones de la entrega

**Línea base:** JDK 11 y Spring Boot 2.7.11. **Versiones finales validadas en HU-007:** Java 25, Spring Boot 4.1.2 y Maven 3.10.0, verificadas en la [migración ejecutada](#migración-ejecutada-hu-007).

| Componente | Línea base citada | Versión final validada | Evidencia |
|---|---|---|---|
| Java | 11 | 25 (OpenJDK 25.0.4.1) | `java -version` y `clean install` del [resultado](#migración-ejecutada-hu-007) |
| Spring Boot | 2.7.11 | 4.1.2 | BOM del POM padre, arranque de REST y CLI |
| Maven | Por registrar | 3.10.0, fijado con el Wrapper | `./mvnw -version` |
| Springdoc | 1.7.0 en REST | `springdoc-openapi-starter-webmvc-ui` 3.2.0 | `/api-docs` publica OpenAPI 3.1.0 y Swagger UI responde 200 |
| Driver MariaDB | 3.1.4 (POM Maria) | 3.1.4, gestionado en el padre | Árbol de dependencias y GET/POST contra MariaDB |
| Starter MongoDB | 2.7.11 (POM Mongo; no equivale a versión del servidor ni del driver transitivo) | Starter 4.1.2 con driver `mongodb-driver-sync` 5.8.1 | Árbol de dependencias y GET/POST contra MongoDB |
| Hibernate | 5.6.15.Final (transitivo) | 7.4.13.Final | Árbol de dependencias y lecturas por JPA |
| Jakarta Persistence | 2.2.3 (`javax.*`) | 3.2.0 (`jakarta.*`) | Imports migrados en el adaptador de MariaDB |
| Lombok | 1.18.26 | 1.18.48, declarado como procesador | `annotationProcessorPaths` del POM padre |


## Declaración frente a entorno observado

POM padre: Spring Framework 5.3.27, Lombok 1.18.26, Log4j-to-SLF4J 2.20.0. CLI declara JAXB API 2.3.1 y core/impl 2.3.0.1. En la inspección inicial, previa a HU-003, se observó OpenJDK 25.0.4.1 y Maven no estaba en el PATH. No hay Wrapper ni pruebas en src/test. Esto no demuestra compatibilidad de la plantilla con Java 25.

## Ambiente de línea base verificado (HU-003)

Ambiente preparado el 2026-11-08 en Windows 11 sobre el commit `9d44066`. Estas versiones son la línea base JDK 11. La columna *Versión final validada* se completa con la entrega.

| Componente | Versión observada | Evidencia |
|---|---|---|
| JDK | Temurin 11.0.32.1 | `java -version` |
| Maven | 3.9.9 | `mvn -version` (detecta Java 11.0.32.1) |
| MariaDB (servidor) | 11.4.13 | `SELECT VERSION();` en el contenedor `mariadb:11.4` |
| MongoDB (servidor) | 6.0.28 | `db.version()` en el contenedor `mongo:6.0` |
| Spring Boot / Spring Framework | 2.7.11 / 5.3.27 | `mvn dependency:list` |
| Springdoc | 1.7.0 | `mvn dependency:list` |
| Driver MariaDB | 3.1.4 | `mvn dependency:list` |
| Driver MongoDB (sync) | 4.6.1 | `mvn dependency:list` |

`mvn -B clean install -DskipTests` termina con `BUILD SUCCESS` en los ocho artefactos del reactor con JDK 11. El 2026-11-09, HU-005 repitió el build y arrancó REST y CLI con Temurin 11.0.33 y Maven 3.9.14 ([arranque](ejecucion-local.md#arranque-de-rest-y-cli-hu-005)).

Se eligió MongoDB 6.0 porque es la versión de servidor más alta que soporta oficialmente el driver Java 4.6, incluido con Spring Boot 2.7.11. MariaDB 11.4 es la versión LTS vigente; el POM no fija la versión del servidor. El procedimiento está en [ejecución local](ejecucion-local.md#ambiente-local-verificado-hu-003).

## Migración ejecutada (HU-007)

Ejecutada el 12 de noviembre de 2026 en macOS ARM64, a partir de la plantilla reparada `55c957f`, en seis commits que se pueden revisar por separado: cinco de construcción y migración y uno de documentación.

| Paso | Commit | Resultado |
|---|---|---|
| Empaquetado, aplanado y Wrapper | `3657037` | `repackage` con clasificador `exec`, `flatten-maven-plugin` 1.9.0 en `resolveCiFriendliesOnly`, Maven Wrapper 3.10.0 y `.gitattributes` que fija `mvnw` en LF |
| BOM de Boot centralizado, sin cambiar de versión | `145d51c` | Árbol de 104 a 102 artefactos; Log4j y el driver MariaDB quedan gestionados en el padre |
| Checkpoint Boot 3.5.16 con JDK 17 | `765e9ec` | Migración a `jakarta.*`, Springdoc 2.10.0 y retiro de JAXB; árbol de 102 a 106 |
| Boot 4.1.2 con Java 25 | `9f9aa15` | `spring-boot-starter-webmvc`, Springdoc 3.2.0 y procesador de Lombok explícito; árbol de 106 a 123 |
| Correcciones de ejecución | `c655a31` | `-parameters` y conexión a MongoDB por `spring.mongodb.uri` |

### Incompatibilidades encontradas y su solución

| Síntoma | Causa | Solución |
|---|---|---|
| `cannot find symbol: variable log` y getters ausentes al compilar con JDK 25 | Desde JDK 23 el procesamiento implícito de anotaciones está desactivado, y la plantilla no hereda de `spring-boot-starter-parent` | `proc=full` y `annotationProcessorPaths` con Lombok 1.18.48 en `maven-compiler-plugin` |
| Los GET respondían 500: `Name for argument of type [java.lang.String] not specified … use the '-parameters' flag` | Al importar el BOM en lugar de heredar del starter parent, nadie activaba `-parameters` | `<parameters>true</parameters>` en `maven-compiler-plugin` |
| MongoDB respondía 500 con `command find requires authentication` (error 13) | Boot 4 dejó de tomar la conexión de `spring.data.mongodb.host`, `port`, `username`, `password` y `authentication-database` | `spring.mongodb.uri` con `authSource=admin` en REST y CLI |
| El BOM bajaba `log4j-api` a 2.17.2 frente al puente 2.20.0, y `mariadb-java-client` a 3.0.10 en REST y CLI | Un BOM importado no ve las propiedades del proyecto que lo importa, así que `log4j2.version` no sirve | Gestión explícita de ambos en el POM padre durante el checkpoint; al llegar a Boot 4 el BOM ya trae Log4j alineado |

### Verificación con ambos motores

Con `mariadb:11.4` y `mongo:6.0` en contenedores y los scripts DDL/DML del repositorio, sobre OpenJDK 25.0.4.1 y Maven 3.10.0:

- `./mvnw -B -ntp clean install` termina en `BUILD SUCCESS` en el padre y los siete módulos, con `Tests run: 6, Failures: 0, Errors: 0` en `PersonaMapperRestTest`.
- `GET /api/v1/persona/MARIA` y `/MONGO` responden `200` con las cinco personas de cada motor.
- `POST /api/v1/persona` guarda en el motor elegido: `cc` 777001 en MariaDB y 777002 en MongoDB, ambos con género `F` y edad 28 comprobados en la base; un alta sin edad guarda `NULL`.
- La CLI lista las personas de MariaDB y de MongoDB en la misma sesión, sin reiniciar, y termina con código 0.
- `/api-docs` publica OpenAPI 3.1.0 con las dos rutas y el parámetro `database`, y Swagger UI responde `200`.
- Un proyecto externo que importa `co.edu.javeriana.as.personapp.mapper.PersonaMapperRest` compila resolviendo el artefacto desde el repositorio local.
- Con un repositorio local vacío (`-Dmaven.repo.local`), los POM instalados de los siete módulos quedan sin `${revision}`. Solo persiste `${global.springframework.boot.version}` en la versión del `spring-boot-maven-plugin` de REST y CLI, y el POM del padre conserva sus propias propiedades, que es donde se declaran.

Los siete módulos, sus responsabilidades y la dirección de las dependencias no cambian. La migración no toca puertos, casos de uso ni adaptadores salvo los imports de `javax.*` a `jakarta.*` en el adaptador de MariaDB.

### Pendientes registrados

- **Defecto ajeno a esta HU, corregido en #47.** Con una persona que tuviera estudios o teléfonos, `GET /api/v1/persona/MARIA` y `/MONGO` fallaban con `StackOverflowError` porque los mappers de Persona, Estudios y Teléfono se llamaban mutuamente sin corte. Era un defecto de la plantilla, no de la migración. Desde #47, los scripts DML cargan además dos profesiones, tres teléfonos y dos estudios en cada motor.
- Conviven Jackson 3.1.7 y Jackson 2.21.7 por una ruta transitiva. Falta decidir si se excluye la generación 2 al verificar la serialización de la API.

## Procedimiento de actualización (HU-007)

### Estado y referencia del inventario

El 10 de noviembre de 2026, `origin/main` del fork apuntaba a [`f5aec999a029127858d0d3b23e450c4f8af38d7d`](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/commit/f5aec999a029127858d0d3b23e450c4f8af38d7d). La [HU-006 (#6)](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/issues/6) seguía abierta y el [PR #38](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/pull/38) seguía sin fusionar. El 11 de noviembre se fusionó en [`55c957f`](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/commit/55c957f232ebd0079eab0d9f021c5d9be9e43128) y la issue quedó cerrada, así que la [HU-007 (#40)](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/issues/40) ya puede ejecutarse y parte de ese commit. Esta inspección caracteriza la plantilla **anterior** a la corrección.

### POM padre y siete módulos

| POM | Dependencias directas y responsabilidad |
|---|---|
| Padre | Agrega los siete módulos, compila para Java 11 con `source` y `target`, y usa `${revision}`. Declara Lombok 1.18.26 (`provided`) y el puente Log4j-to-SLF4J 2.20.0, heredados por los siete. Fija las versiones de Boot y Framework en propiedades y carece de BOM central de Boot. |
| `common` | Soporte compartido. Spring Context 5.3.27 directo, con exclusiones de AOP, JCL y Expression en esa ruta. |
| `domain` | Modelos. Solo las dependencias heredadas. |
| `application` | Puertos y casos de uso. Depende de `domain` y `common`. |
| `maria-output-adapter` | Depende de `application`, starter Data JPA y Connector/J 3.1.4. Hibernate, Jakarta Persistence y transacciones llegan por el starter. |
| `mongo-output-adapter` | Depende de `application` y starter Data MongoDB. Spring Data MongoDB y el driver síncrono, su núcleo y BSON llegan transitivamente. |
| `rest-input-adapter` | Depende de `application`, ambos adaptadores de salida, starter Web y Springdoc 1.7.0. Jackson, Spring MVC y Tomcat son transitivos. Declara el plugin Boot sin ejecución `repackage`. |
| `cli-input-adapter` | Depende de `application`, ambos adaptadores de salida, starter Boot y JAXB legado. Declara una propiedad de fábrica JAXB y el plugin Boot sin ejecución `repackage`. |

### Evidencia ejecutada antes de HU-006

Desde un archivo del commit extraído en un directorio temporal, con Homebrew OpenJDK 11.0.31 y Maven 3.9.14 en macOS ARM64, se ejecutó en este orden. En los comandos, `mvn` representa el ejecutable Maven 3.9.14 y `$audit_dir` el directorio temporal.

```sh
mvn -version
mvn -B -ntp -Dmaven.repo.local="$audit_dir/maven-repo" clean install > baseline-build.log 2>&1
mvn -B -ntp -Dmaven.repo.local="$audit_dir/maven-repo" dependency:tree -Dverbose > baseline-tree.log 2>&1
```

Ambos comandos Maven terminaron con código 0 y `BUILD SUCCESS` en los siete módulos y el padre. Surefire informó `No tests to run`. La búsqueda en el árbol Git de ese SHA tampoco encontró archivos en `src/test`. Las seis pruebas que reporta el PR #38 se repitieron después, con el resultado que registra la [evidencia de la línea base reparada](#reproducibilidad-de-la-construcción-y-artefactos-ejecutables). En esta sesión no se ejecutaron los arranques, Swagger ni los motores. La evidencia previa de HU-005 está en [ejecución local](ejecucion-local.md#arranque-de-rest-y-cli-hu-005).

Extracto del árbol resuelto. Las ramas repetidas y los nodos omitidos por Maven están abreviados.

```text
common -> spring-context 5.3.27 -> spring-core 5.3.27
maria-output-adapter -> starter-data-jpa 2.7.11
  -> spring-data-jpa 2.7.11, hibernate-core 5.6.15.Final
  -> jakarta.persistence-api 2.2.3, jakarta.transaction-api 1.3.3
  -> jaxb-runtime 2.3.1 (vía Hibernate)
maria-output-adapter -> mariadb-java-client 3.1.4
mongo-output-adapter -> starter-data-mongodb 2.7.11
  -> spring-data-mongodb 3.4.11, mongodb-driver-sync 4.6.1
rest-input-adapter -> starter-web 2.7.11
  -> jackson-databind 2.13.5, tomcat-embed-core 9.0.74
rest-input-adapter -> springdoc-openapi-ui 1.7.0
cli-input-adapter -> starter 2.7.11
cli-input-adapter -> jaxb-api 2.3.1, jaxb-core 2.3.0.1, jaxb-impl 2.3.0.1
padre y módulos -> lombok 1.18.26, log4j-to-slf4j 2.20.0
  -> log4j-api 2.20.0, slf4j-api 1.7.36
```

Plugins observados con ese ejecutable Maven: Clean 3.2.0, Compiler 3.15.0, Resources 3.4.0, Surefire 3.5.4, Jar 3.5.0, Install 3.1.4 y Dependency 3.7.0. Boot Maven Plugin 2.7.11 consta en los dos POM de entrada. El árbol de dependencias de aplicación no incluye las dependencias de los plugins. En la línea base reparada, guardar también el POM efectivo y, cuando se necesite auditar las dependencias de plugins, la salida de `dependency:resolve-plugins`.

### Matriz de compatibilidad propuesta

Versiones candidatas al 10 de noviembre de 2026. La documentación y el BOM confirman las versiones publicadas y la compatibilidad indicada. La combinación completa sigue pendiente de ejecutar en PersonAPP.

| Componente | Versión actual | Versión propuesta | Compatibilidad | Cambios necesarios | Fuente |
|---|---|---|---|---|---|
| Java | 11, compilado con 11.0.31 | Temurin 25.0.5+8, Java 25 LTS | Java 25 es la LTS más alta publicada. Boot 4.1 admite Java 25. | Usar `release=25`, habilitar el procesamiento explícito de Lombok y comprobar `java -version` y `mvn -version`. | [Oracle](https://www.oracle.com/java/technologies/java-se-support-roadmap.html), [Temurin](https://adoptium.net/temurin/releases/?os=any&package=jdk), [requisitos de Boot](https://docs.spring.io/spring-boot/4.1/system-requirements.html) |
| Maven | 3.9.14 observado, sin Wrapper | 3.10.0, ya ejecutado sobre la línea base reparada | Serie GA mantenida. Maven 4 sigue en RC al corte. | Probar desde caché limpia y comprobar resolución del reactor con `${revision}`. | [Apache Maven](https://maven.apache.org/docs/history) |
| Spring Boot y Spring Framework | Boot 2.7.11, Framework 5.3.27 | Boot 4.1.2 y Framework 7.0.10 | Boot 4.1.2 es GA y su BOM fija Framework 7.0.10. Boot 4.2 y Framework 7.1 estaban en RC. | Centralizar el BOM en el padre. Sustituir starter Web por `spring-boot-starter-webmvc` y alinear los starters y módulos Spring. | [Release Boot](https://spring.io/blog/2026/10/23/spring-boot-4-1-2-available-now/), [BOM 4.1.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.2/spring-boot-dependencies-4.1.2.pom), [Framework](https://docs.spring.io/spring/reference/7.1/index.html) |
| Spring Data, Hibernate y Jakarta | JPA 2.7.11, MongoDB 3.4.11, Hibernate 5.6.15.Final, Persistence 2.2.3 y Transaction 1.3.3 | Spring Data BOM 2026.0.2 y Hibernate 7.4.13.Final del BOM Boot | Boot 4 usa Jakarta. Las APIs 2.2.3 y 1.3.3 de la plantilla todavía exponen paquetes `javax.*`. | Migrar `javax.persistence` y `javax.transaction` a `jakarta.*`. Verificar tipos, consultas nombradas, IDs, relaciones, fechas, enums y validación del esquema. | [BOM 4.1.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.2/spring-boot-dependencies-4.1.2.pom), [guía Boot 4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) |
| Springdoc y JSON | `springdoc-openapi-ui` 1.7.0, Jackson 2.13.5, Tomcat 9.0.74 | `springdoc-openapi-starter-webmvc-ui` 3.2.0, Jackson 3.1.7 y Tomcat 11.0.27 del BOM | Springdoc 3.2.0 se construyó sobre Boot 4.1.2. Jackson cambia de generación. | Verificar `@JsonFormat`, formatos, rutas, `/api-docs`, Swagger UI y parámetros con `-parameters`. Revisar cualquier Jackson 2 que introduzca Springdoc antes de excluirlo. | [Springdoc 3.2.0](https://github.com/springdoc/springdoc-openapi/releases/tag/v3.2.0), [guía Boot 4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) |
| Lombok | 1.18.26 `provided` | 1.18.48 | Lombok declara soporte de JDK 25 desde 1.18.40. | Mantenerlo fuera del runtime y declararlo como procesador; comprobar generación y anotaciones `@NonNull`. | [Lombok](https://projectlombok.org/changelog) |
| JAXB | API 2.3.1, core/impl 2.3.0.1 en CLI; runtime transitivo 2.3.1 | Retirar declaraciones legadas de CLI si la prueba confirma que son prescindibles. Si hay uso real, API Jakarta 4.0.5 y GlassFish 4.0.9 del BOM | La inspección del código en el SHA no encontró imports JAXB. Hibernate puede conservar su runtime transitivo. | Quitar propiedad de fábrica interna y verificar arranque y árbol. Migrar a `jakarta.xml.bind` solo si se confirma un consumidor. | [BOM 4.1.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.2/spring-boot-dependencies-4.1.2.pom), [Jakarta XML Binding](https://jakarta.ee/specifications/xml-binding/4.0/) |
| MariaDB Connector/J y servidor | Driver 3.1.4, servidor de prueba 11.4.13 | Connector/J 4.0.0 y MariaDB 12.3.3 LTS en pruebas | El driver GA requiere Java 17 o superior y cambia los valores de TLS. 12.3 es la línea LTS más alta publicada. | Probar autenticación, certificados y consultas. Si el entorno de prueba no tiene TLS, configurar ese perfil de forma explícita y conservar la verificación de certificados en conexiones TLS. | [Connector/J](https://mariadb.org/connector-java/all-releases/), [TLS y Java](https://mariadb.org/mariadb-foundation-newsletter-1-november-2026/), [MariaDB LTS](https://mariadb.org/mariadb-server-12-3-lts-released/), [parches](https://mariadb.org/mariadb/all-releases/) |
| Driver MongoDB y servidor | Driver sync 4.6.1 transitivo, servidor de prueba 6.0.28 | Driver sync/core/BSON 5.13.0 y servidor 9.0, parche 9.0.8 publicado al corte | MongoDB documenta compatibilidad completa del driver 5.13 con servidor 9.0. El 5.8.1 del BOM Boot no tiene compatibilidad documentada con 9.0. Falta probar el ajuste del driver con Spring Data y Java 25. | Alinear todos los artefactos MongoDB. Migrar la conexión de ambos adaptadores de entrada a `spring.mongodb.uri` con `authSource=admin`. Confirmar disponibilidad del parche para el entorno de pruebas. | [Driver](https://github.com/mongodb/mongo-java-driver/releases), [compatibilidad](https://www.mongodb.com/docs/drivers/compatibility/?driver-language=java&java-driver-framework=java-sync), [servidor](https://www.mongodb.com/docs/manual/release-notes/9.0/), [guía Boot 4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) |
| Logging | Puente y API Log4j 2.20.0, SLF4J 1.7.36, Logback 1.2.12 | Puente/API Log4j 2.26.1; SLF4J 2 y Logback alineados por BOM | La plantilla usa anotaciones de logging. Comprobar el proveedor efectivo y evitar puentes en ambas direcciones. | Alinear puente y API como unidad. Confirmar que arranca un único proveedor SLF4J y conserva los mensajes. | [Apache Log4j](https://logging.apache.org/log4j/2.x/download.cgi), [BOM 4.1.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.2/spring-boot-dependencies-4.1.2.pom) |
| Plugins y pruebas | Boot 2.7.11, Compiler 3.15.0, Surefire 3.5.4, Dependency 3.7.0; JUnit 5.8.2 en PR #38 | Boot plugin 4.1.2, Compiler 3.16.0, Surefire 3.6.1, Dependency 3.11.0, JUnit 6.1.3 | Releases estables publicadas. JUnit 6 requiere Java 17 o posterior y el BOM Boot propone 6.0.3. | Fijar plugins del reactor, alinear JUnit Jupiter y Platform con un solo BOM, comprobar que Surefire ejecuta los seis casos y evitar los plugins 4.x en beta. | [Plugins Maven](https://maven.apache.org/plugins/), [JUnit](https://docs.junit.org/6.1.3/release-notes.html), [plugin Boot](https://docs.spring.io/spring-boot/maven-plugin/using.html) |

La propuesta parte del BOM de Boot y ensaya en pasos separados los cambios de versión de MariaDB, MongoDB, Log4j y JUnit frente a sus valores administrados. Una diferencia respecto al BOM requiere verificación del árbol y ejecución, y no se considera una incompatibilidad comprobada por sí sola. Las series de servidor de esta matriz se usarán para pruebas de compatibilidad con datos desechables. La definición de imágenes y servicios de entrega corresponde a HU-034.

### Reproducibilidad de la construcción y artefactos ejecutables

Tres defectos de construcción quedan dentro de HU-007, porque se corrigen en los POM que ya toca la actualización. Se acordaron en el chat del equipo con el responsable de HU-034, que necesita los JAR ejecutables.

| Defecto | Comprobación ejecutada | Corrección prevista |
|---|---|---|
| Los JAR de REST y CLI no son ejecutables | El manifiesto de `rest-input-adapter-0.0.1-SNAPSHOT.jar` y de `cli-input-adapter-0.0.1-SNAPSHOT.jar` no declara `Main-Class` ni `Start-Class`, y ninguno contiene entradas `BOOT-INF` | Declarar la ejecución `repackage` del Boot Maven Plugin en los dos POM de entrada |
| El POM instalado publica propiedades sin resolver | El POM de `rest-input-adapter` instalado en `~/.m2` conserva `<version>${revision}</version>` y `${global.springframework.boot.version}` | Agregar `flatten-maven-plugin` 1.9.0 en el padre. Resuelve las coordenadas propias y del padre y las versiones de las dependencias entre módulos, que es lo que impedía consumir los artefactos. `resolveCiFriendliesOnly` solo resuelve `revision`, `sha1` y `changelist`, así que `${global.springframework.boot.version}` permanece en la versión del `spring-boot-maven-plugin` de REST y CLI; esa sección no la lee quien consume el artefacto. Con 1.6.0 quedaba además `${revision}` en las dependencias entre módulos |
| El repositorio no fija la versión de Maven | No hay `mvnw` ni `.mvn/wrapper`; cada integrante usa el Maven que tenga instalado, y en esta sesión fue 3.10.0 frente al 3.9.14 del inventario | Añadir el Maven Wrapper con la versión elegida en la matriz, para que HU-034 construya sin instalar Maven en la imagen, y un `.gitattributes` con `mvnw text eol=lf`, porque con `core.autocrlf=true` el checkout en Windows deja el script en CRLF y entonces `./mvnw` falla en Linux con `sh: 1: ./mvnw: not found` |

**Evidencia de la línea base reparada.** El 11 de noviembre de 2026 se ejecutó el head de la [HU-006 (#38)](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/pull/38), commit `10465ff`, en macOS ARM64 con Homebrew OpenJDK 11.0.31 y Maven 3.10.0.

```sh
mvn -B -ntp -pl rest-input-adapter -am -Dtest=PersonaMapperRestTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -B -ntp clean install
```

`PersonaMapperRestTest` ejecutó `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` y el reactor terminó en `BUILD SUCCESS` sobre el padre y los siete módulos. Esa ejecución confirma las seis pruebas que reporta el PR y cierra el pendiente de repetirlas. Las tres comprobaciones de la tabla anterior se tomaron de ese mismo build.

### Orden y comprobaciones pendientes

1. **Capturar la plantilla reparada.** El #38 ya está en `origin/main`: la rama de migración parte de `55c957f`, que es el SHA de referencia para volver atrás. Con JDK 11, guardar `java -version`, `mvn -version`, `mvn -B -ntp clean install`, `mvn -B -ntp dependency:tree -Dverbose` y `mvn -B -ntp help:effective-pom`. Registrar los informes Surefire y los seis casos de `PersonaMapperRestTest`. Conservar esa revisión como referencia de vuelta atrás. Sobre la versión de Maven: el inventario de la plantilla se tomó con 3.9.14 y la verificación de la línea base reparada con 3.10.0, ambas de la serie GA mantenida. La captura registra la que se use, y el Maven Wrapper de esta HU retira esa diferencia, porque a partir de entonces el reactor se construye con la versión que fija el repositorio y no con la instalada en cada máquina.
2. **Alinear la construcción.** Introducir el BOM y los plugins de forma controlada. Hacer checkpoints de Boot 3 como puente para Jakarta, y luego Boot 4.1 con JDK 25. Comprobar en cada checkpoint los siete módulos, las pruebas y el árbol. Revisar con `git diff` que permanezcan los siete módulos y las mismas relaciones entre los POM. Si se cambia la resolución de `${revision}` o el empaquetado, verificar instalación con caché limpia y arranque de ambas entradas. En este paso entran `repackage`, `flatten-maven-plugin` y el Maven Wrapper: comprobar que el JAR de cada entrada arranca con `java -jar`, que el POM instalado no deja propiedades sin resolver y que `./mvnw -version` informa la versión fijada.
3. **Migrar los adaptadores.** Revisar JPA/Jakarta, configuración de MongoDB, JAXB, logging, JSON y Springdoc. Aplicar las versiones candidatas fuera del BOM una a una y comparar el árbol después de cada ajuste. Conservar puertos, adaptadores y la dirección de dependencias. Mantener los CRUD y la contenerización fuera del cambio.
4. **Repetir la regresión y probar ambas entradas.** Ejecutar `mvn -B -ntp clean install` y, para aislar el mapper, `mvn -B -ntp -pl rest-input-adapter -am -Dtest=PersonaMapperRestTest -Dsurefire.failIfNoSpecifiedTests=false test`. Confirmar ejecución real de seis casos. Cargar DDL/DML en instancias desechables de ambos motores y verificar las cinco personas con las credenciales de aplicación. Arrancar REST y CLI y comprobar que cada entrada consulta los dos motores. En REST comprobar los GET por motor, el POST del caso de regresión con un ID de prueba y su persistencia en el motor seleccionado. Revisar atributos e identificadores y comprobar el mapeo de relaciones con datos de prueba cuando la línea base lo permita. Probar `/api-docs` y Swagger UI, incluidos esquema y parámetros.
5. **Cerrar la evidencia para HU-034.** Guardar los SHA inicial y final, los dos árboles completos, los POM efectivos, los resultados, versiones de servidor, configuración, clases o artefactos de arranque, puertos y cualquier error con su solución. Enlazar este procedimiento desde el README. Si falla un paso, reproducirlo desde el último checkpoint que pasó y registrar el fallo antes de avanzar. Usar bases desechables separadas para que el rollback del código no dependa de revertir datos ni esquemas.

**Pendiente:** compilación y pruebas con el stack candidato, JAR ejecutables y POM instalado sin propiedades sin resolver, arranque de REST y CLI, conexión con ambos motores, Swagger, árbol final y enlace desde el README. HU-034 puede usar esta matriz para preparar sus pruebas cuando se confirmen las versiones finales.
