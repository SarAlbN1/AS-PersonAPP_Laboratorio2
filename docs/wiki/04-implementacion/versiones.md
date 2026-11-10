# Versiones de la entrega

**Entrega:** conservar JDK 11 y verificar la plantilla.

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

`mvn -B clean install -DskipTests` termina con `BUILD SUCCESS` en los ocho artefactos del reactor con JDK 11. No se arrancaron REST ni CLI; ese paso corresponde a HU-005.

Se eligió MongoDB 6.0 porque es la versión de servidor más alta que soporta oficialmente el driver Java 4.6, incluido con Spring Boot 2.7.11. MariaDB 11.4 es la versión LTS vigente; el POM no fija la versión del servidor. El procedimiento está en [ejecución local](ejecucion-local.md#ambiente-local-verificado-hu-003).
