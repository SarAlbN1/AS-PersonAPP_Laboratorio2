# Versiones de la entrega

**Línea base:** JDK 11. HU-007 seleccionará y verificará las versiones finales de entrega.

| Componente | Línea base citada | Versión final validada | Evidencia |
|---|---|---|---|
| Java | 11 | Pendiente | `java -version` |
| Spring Boot | 2.7.11 | Pendiente | POM y compilación |
| Maven | Por registrar | Pendiente | `mvn -version` |
| Springdoc | 1.7.0 en REST | Pendiente | POM/Swagger |
| Driver MariaDB | 3.1.4 (POM Maria) | Pendiente | POM/prueba |
| Starter MongoDB | 2.7.11 (POM Mongo; no equivale a versión del servidor ni del driver transitivo) | Pendiente | POM/prueba |


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

## Procedimiento de actualización (HU-007)

### Estado y referencia del inventario

El 10 de noviembre de 2026, `origin/main` del fork apuntaba a [`f5aec999a029127858d0d3b23e450c4f8af38d7d`](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/commit/f5aec999a029127858d0d3b23e450c4f8af38d7d). La [HU-006 (#6)](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/issues/6) seguía abierta y el [PR #38](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/pull/38), en `570f34e`, seguía sin fusionar. La implementación de la [HU-007 (#40)](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2/issues/40) espera esa integración. Esta inspección caracteriza la plantilla **anterior** a la corrección.

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

Ambos comandos Maven terminaron con código 0 y `BUILD SUCCESS` en los siete módulos y el padre. Surefire informó `No tests to run`. La búsqueda en el árbol Git de ese SHA tampoco encontró archivos en `src/test`. El PR #38 reporta seis pruebas, pendientes de repetir cuando estén en `main`. En esta sesión no se ejecutaron los arranques, Swagger ni los motores. La evidencia previa de HU-005 está en [ejecución local](ejecucion-local.md#arranque-de-rest-y-cli-hu-005).

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
| Maven | 3.9.14 observado, sin Wrapper | 3.10.0 | Serie GA mantenida. Maven 4 sigue en RC al corte. | Probar desde caché limpia y comprobar resolución del reactor con `${revision}`. | [Apache Maven](https://maven.apache.org/docs/history) |
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

### Orden y comprobaciones pendientes

1. **Capturar la plantilla reparada.** Una vez fusionado el #38, verificar que su cambio está en `origin/main`, registrar el nuevo SHA y crear la rama de migración desde allí. Con JDK 11 y Maven 3.9.14, guardar `java -version`, `mvn -version`, `mvn -B -ntp clean install`, `mvn -B -ntp dependency:tree -Dverbose` y `mvn -B -ntp help:effective-pom`. Registrar los informes Surefire y los seis casos de `PersonaMapperRestTest`. Conservar esa revisión como referencia de vuelta atrás.
2. **Alinear la construcción.** Introducir el BOM y los plugins de forma controlada. Hacer checkpoints de Boot 3 como puente para Jakarta, y luego Boot 4.1 con JDK 25. Comprobar en cada checkpoint los siete módulos, las pruebas y el árbol. Revisar con `git diff` que permanezcan los siete módulos y las mismas relaciones entre los POM. Si se cambia la resolución de `${revision}` o el empaquetado, verificar instalación con caché limpia y arranque de ambas entradas.
3. **Migrar los adaptadores.** Revisar JPA/Jakarta, configuración de MongoDB, JAXB, logging, JSON y Springdoc. Aplicar las versiones candidatas fuera del BOM una a una y comparar el árbol después de cada ajuste. Conservar puertos, adaptadores y la dirección de dependencias. Mantener los CRUD y la contenerización fuera del cambio.
4. **Repetir la regresión y probar ambas entradas.** Ejecutar `mvn -B -ntp clean install` y, para aislar el mapper, `mvn -B -ntp -pl rest-input-adapter -am -Dtest=PersonaMapperRestTest -Dsurefire.failIfNoSpecifiedTests=false test`. Confirmar ejecución real de seis casos. Cargar DDL/DML en instancias desechables de ambos motores y verificar las cinco personas con las credenciales de aplicación. Arrancar REST y CLI y comprobar que cada entrada consulta los dos motores. En REST comprobar los GET por motor, el POST del caso de regresión con un ID de prueba y su persistencia en el motor seleccionado. Revisar atributos e identificadores y comprobar el mapeo de relaciones con datos de prueba cuando la línea base lo permita. Probar `/api-docs` y Swagger UI, incluidos esquema y parámetros.
5. **Cerrar la evidencia para HU-034.** Guardar los SHA inicial y final, los dos árboles completos, los POM efectivos, los resultados, versiones de servidor, configuración, clases o artefactos de arranque, puertos y cualquier error con su solución. Enlazar este procedimiento desde el README. Si falla un paso, reproducirlo desde el último checkpoint que pasó y registrar el fallo antes de avanzar. Usar bases desechables separadas para que el rollback del código no dependa de revertir datos ni esquemas.

**Pendiente:** línea base con HU-006 integrada, compilación y pruebas con el stack candidato, arranque de REST y CLI, conexión con ambos motores, Swagger, árbol final y enlace desde el README. HU-034 puede usar esta matriz para preparar sus pruebas cuando se confirmen las versiones finales.
