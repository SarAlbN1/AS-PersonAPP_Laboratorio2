# Registro de evidencias

| ID | Actividad | Comando / procedimiento | Resultado real | Commit / archivo / captura | Estado |
|---|---|---|---|---|---|
| EV-01 | Fork del profesor | Registrar URL | Pendiente | Pendiente | No ejecutado |
| EV-02 | Java y Maven | `java -version`, `mvn -version` | Pendiente | Pendiente | No ejecutado |
| EV-03 | DDL/DML MariaDB y MongoDB | Ejecutar scripts de referencia | Pendiente | Pendiente | No ejecutado |
| EV-04 | Compilación baseline | `mvn clean install` | Pendiente | Pendiente | No ejecutado |
| EV-05 | REST, CLI y Swagger | Arranques originales con JDK 11 ([procedimiento](ejecucion-local.md#arranque-de-rest-y-cli-hu-005)) | REST y CLI arrancan. Los GET por motor devuelven 200, el POST devuelve 500 y Swagger responde 200 | Commit `f727fcd`; [resultados](ejecucion-local.md#arranque-de-rest-y-cli-hu-005) | Ejecutado (HU-005) |
| EV-06 | Depuración Mapper | Reproducción y regresión ([procedimiento](ejecucion-local.md#corrección-del-mapper-rest-hu-006)) | `PersonaMapperRest.fromAdapterToDomain` corregido. Seis pruebas de regresión pasan y el POST guarda en MariaDB y MongoDB | Rama `fix/HU-006-mapper-rest` sobre `e1a360d`; [resultados](ejecucion-local.md#corrección-del-mapper-rest-hu-006) | Ejecutado (HU-006) |
| EV-07 | Migración de versiones | Compilación y tests | Pendiente | Pendiente | No ejecutado |
| EV-08 | Matriz CRUD | REST/CLI × MariaDB/MongoDB | Pendiente | Pendiente | No ejecutado |
| EV-09 | Cambio de base en caliente | Alternar motores sin reinicio | Pendiente | Pendiente | No ejecutado |
| EV-10 | Tag final | URL y SHA | Pendiente | Pendiente | No ejecutado |
