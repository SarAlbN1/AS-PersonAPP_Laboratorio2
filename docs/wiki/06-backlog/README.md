# Backlog único del Laboratorio 2

La entrega comprende un servicio web hexagonal con Repository y Service, JDK 11, Spring Boot, MariaDB, MongoDB, REST, CLI y Swagger 3. El repositorio etiquetado debe incluir el código fuente, scripts DDL/DML y un README reproducible. El informe incluye portada, marco conceptual, diseño, procedimiento, conclusiones y lecciones aprendidas, y referencias.

El equipo definió seis operaciones para Persona, Profesión, Teléfono y Estudios: **crear, listar todos, listar parcial, editar, eliminar y contar**. El listado parcial usa paginación y orden estable. El conteo devuelve el total del motor elegido. La selección dinámica de motor es un criterio del equipo.

**Punto de partida.** Persona ya tiene puertos, caso de uso, repositorios y adaptadores para ambos motores. REST declara un listado y un alta cuyo mapper está incompleto. CLI permite listar. El conteo existe como método interno. Profesión, Teléfono y Estudios ya tienen modelos, representaciones de persistencia y mappers. Sus operaciones por REST y CLI siguen pendientes.

**Orden de entrega.** Reproducir JDK 11 y corregir los mapeos. Acordar los contratos y completar Persona de punta a punta. Continuar con Profesión, Teléfono y Estudios. Verificar las operaciones desde ambas entradas y motores, cerrar Swagger y el README, completar el informe y publicar el tag.

Los estados reflejan la inspección del repositorio. Para cerrar una historia, registrar el resultado observado, el comando o solicitud y el commit correspondiente. Aplicar la [definición de terminado](../05-proceso/dor-y-dod.md).

## Hitos de entrega

Un **milestone** agrupa historias por resultado verificable. Una **épica** agrupa historias por objetivo de trabajo. Cada HU de la entrega pertenece a un hito y a una épica. Los IDs son locales. La creación de milestones e issues en GitHub se gestiona aparte.

| Milestone | Épicas | Depende de | Criterio de salida |
|---|---|---|---|
| M1 · Línea base reproducible | EP-01 | — | Preparación y arranques en JDK 11, acceso a ambos motores y regresión de mapeo. |
| M2 · Operaciones de los cuatro recursos | EP-03 | M1 | Seis operaciones por recurso en REST y CLI, con persistencia en ambos motores. |
| M3 · Verificación y entrega | EP-04 y EP-05 | M2 | Evidencias, Swagger 3, README reproducible, informe y URL del tag. |

**Prioridad.** P0 identifica el camino de entrega. P1 identifica apoyo interno o trabajo diferido. El informe y la auditoría pueden avanzar en paralelo con la implementación. Su cierre depende de la evidencia de las historias previas. El equipo asignará responsables antes de iniciar cada HU.

## M1 · Línea base reproducible

### EP-01 · Repositorio, entorno y mapeos

#### HU-001 · Crear fork del repositorio y establecer flujo de colaboración

**Estado:** Parcial. **Dependencias:** Ninguna. **Trazabilidad:** RE-02. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [ ] Existe fork del proyecto docente y URL verificable.
- [ ] README identifica el repositorio base y el fork sin confundirlos.
- [ ] Se prueba el flujo rama → PR de revisión cruzada; no se modificó el original.

#### HU-002 · Inventariar módulos, dependencias y capacidades de la plantilla

**Estado:** Entregable documental listo para revisión. **Dependencias:** HU-001. **Trazabilidad:** RA-01. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [ ] Se registran siete módulos Maven y sus dependencias.
- [ ] Se distingue entre operaciones declaradas, implementadas y expuestas por REST/CLI.
- [ ] Se registran rutas, menús y clases observadas con referencias al código.

#### HU-003 · Preparar y documentar ambiente Java y bases de datos originales

**Estado:** Pendiente. **Dependencias:** HU-001. **Trazabilidad:** RT-01. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [ ] Se registran java -version, mvn -version y versiones reales de MariaDB/MongoDB.
- [ ] Se configuran variables/credenciales locales sin subir secretos.
- [ ] Se documenta el procedimiento desde un clon limpio.

#### HU-004 · Ejecutar DDL/DML inicial en MariaDB y MongoDB

**Estado:** Pendiente. **Dependencias:** HU-003. **Trazabilidad:** RE-02. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [ ] DDL/DML o scripts equivalentes se ejecutan y se registran resultados.
- [ ] Se detecta y resuelve documentadamente discrepancia persona_db/prueba_db en MongoDB.
- [ ] Se comprueban tablas/colecciones y registros de ejemplo.

#### HU-005 · Compilar y ejecutar REST y CLI de la plantilla original

**Estado:** Pendiente. **Dependencias:** HU-002, HU-004. **Trazabilidad:** RT-01. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [ ] mvn clean install termina o se documentan los bloqueos reproducibles.
- [ ] Arranca PersonAppRestApi y se comprueban endpoints iniciales y Swagger.
- [ ] Arranca PersonAppCli y se comprueba menú/listado inicial.
- [ ] Se guardan comandos, resultados y capturas/logs reales.

#### HU-006 · Reproducir, localizar y corregir el defecto del Mapper

**Estado:** Pendiente. **Dependencias:** HU-005. **Trazabilidad:** RF-08. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [ ] La falla se reproduce y se registra entrada, salida y error.
- [ ] La corrección se limita al Mapper defectuoso localizado.
- [ ] Una prueba de regresión demuestra transformación correcta y no se afecta el flujo base.

## M2 · Operaciones de los cuatro recursos

### EP-03 · Contratos y operaciones de dominio

**Persona y contrato común**

#### HU-011 · Definir contratos de dominio, identificadores y validaciones comunes

**Estado:** Parcial. **Dependencias:** HU-002, HU-005. **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] Se documentan llaves de persona, profesión, teléfono y estudios (compuesta).
- [ ] Se define comportamiento para duplicados, FKs, datos inválidos y eliminaciones.
- [ ] REST/CLI y ambos motores acuerdan las mismas reglas funcionales; no se inventan rutas como hechos existentes.
- [ ] Se definen listado total, paginación con orden estable y conteo por motor, además de las reglas de identidad para editar y eliminar.

#### HU-012 · Completar puertos y casos de uso CRUD de Persona

**Estado:** Parcial. **Dependencias:** HU-006, HU-011. **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] Las seis operaciones funcionan por puertos.
- [ ] El caso de uso no importa entidades de MariaDB/MongoDB.
- [ ] Se prueban errores y transformaciones relevantes.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-013 · Completar adaptadores de persistencia de Persona en ambas bases

**Estado:** Parcial. **Dependencias:** HU-012. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] CRUD de persona opera con MariaDB y MongoDB reales.
- [ ] Los mappers preservan identificador y campos.
- [ ] Las pruebas no usan un motor simulado como única evidencia.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-014 · Exponer CRUD de Persona por REST y documentarlo

**Estado:** Parcial. **Dependencias:** HU-013. **Trazabilidad:** RF-02. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] REST ofrece alta, listado total, listado parcial, edición, eliminación y conteo.
- [ ] Estados HTTP, DTO y errores documentados y probados en Swagger/OpenAPI.
- [ ] Se verifica selección de motor por petición según contrato definido.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-015 · Exponer CRUD de Persona por CLI

**Estado:** Parcial. **Dependencias:** HU-013. **Trazabilidad:** RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] El menú ejecuta alta, listado total, listado parcial, edición, eliminación y conteo.
- [ ] Mensajes de éxito/error identificables y validación de entrada.
- [ ] Se ejecuta con cada base sin reiniciar cuando se cambia la opción.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

**Profesión, Teléfono y Estudios**

#### HU-016 · Implementar dominio, puertos y casos de uso de Profesión

**Estado:** Parcial. **Dependencias:** HU-011. **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] Modelo y puertos de profesión implementados.
- [ ] Casos de uso CRUD y validaciones probados.
- [ ] Sin referencias a controladores ni clases de persistencia en dominio.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-017 · Implementar persistencia de Profesión en MariaDB y MongoDB

**Estado:** Parcial. **Dependencias:** HU-016. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] Entidades/documentos, repositorios y mappers disponibles.
- [ ] CRUD verificado en MariaDB y MongoDB reales.
- [ ] Pruebas contemplan clave duplicada y relaciones pertinentes.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-018 · Exponer CRUD de Profesión por REST y CLI

**Estado:** Pendiente. **Dependencias:** HU-017. **Trazabilidad:** RF-02, RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] REST permite CRUD con contrato documentado.
- [ ] CLI permite CRUD de profesión.
- [ ] Ambos funcionan con MariaDB y MongoDB y registran casos negativos.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-019 · Implementar dominio, puertos y casos de uso de Teléfono

**Estado:** Parcial. **Dependencias:** HU-011. **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] Modelo, identificador y referencia a persona validados.
- [ ] Puertos y casos de uso realizan CRUD.
- [ ] Se prueba rechazo de dueño inexistente y conflictos de identificador según reglas acordadas.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-020 · Implementar persistencia de Teléfono en ambos motores

**Estado:** Parcial. **Dependencias:** HU-019, HU-013. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] CRUD funciona sobre tablas/colecciones reales.
- [ ] Relación dueño-persona conserva integridad según cada motor.
- [ ] Pruebas verifican lectura posterior y restricciones.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-021 · Exponer CRUD de Teléfono por REST y CLI

**Estado:** Pendiente. **Dependencias:** HU-020. **Trazabilidad:** RF-02, RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] REST y CLI ejecutan CRUD completo.
- [ ] Se documentan respuestas y errores de dueño inexistente.
- [ ] Se demuestra en MariaDB y MongoDB.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-022 · Implementar dominio, puertos y casos de uso de Estudios

**Estado:** Parcial. **Dependencias:** HU-011. **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] Se usa identidad compuesta (id_prof, cc_per) del modelo base, salvo decisión documentada.
- [ ] Casos de uso CRUD e integridad de dos referencias.
- [ ] Pruebas cubren duplicados y referencias inválidas.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-023 · Implementar persistencia de Estudios en ambos motores

**Estado:** Parcial. **Dependencias:** HU-022, HU-013, HU-017. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] CRUD persiste en ambos motores reales.
- [ ] El Mapper respeta el par identificador y sus referencias.
- [ ] Se prueba persistencia y validación de dependencias.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-024 · Exponer CRUD de Estudios por REST y CLI

**Estado:** Pendiente. **Dependencias:** HU-023. **Trazabilidad:** RF-02, RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [ ] API y menú CLI permiten los CRUD previstos.
- [ ] El contrato representa los dos campos de la clave compuesta.
- [ ] Funcionamiento comprobado en ambos motores y con errores.
- [ ] Se verifican las seis operaciones en la capa que cubre la historia: crear, listar todos, listar parcialmente, editar, eliminar y contar. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

## M3 · Verificación y entrega

### EP-04 · Integración, arquitectura y Swagger

#### HU-025 · Verificar selección dinámica por petición en REST y por menú en CLI

**Estado:** Parcial. **Dependencias:** HU-014, HU-015, HU-018, HU-021, HU-024. **Trazabilidad:** RF-05. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [ ] Durante un único proceso REST, solicitudes sucesivas eligen MariaDB y MongoDB.
- [ ] Durante una única sesión CLI se alternan ambos motores.
- [ ] Cada consulta encuentra sus datos en el motor correspondiente y no cambia globalmente el motor de solicitudes concurrentes.

#### HU-026 · Verificar las seis operaciones desde REST y CLI en ambos motores

**Estado:** Pendiente. **Dependencias:** HU-018, HU-021, HU-024, HU-025. **Trazabilidad:** RF-01, RF-02, RF-03, RF-04. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [ ] Ejecutar las seis operaciones por recurso desde REST y CLI sobre MariaDB y MongoDB. Adjuntar evidencia reproducible de cada flujo.
- [ ] Se prueban casos negativos: invalidez, duplicados, relaciones, inexistentes.
- [ ] Cada resultado tiene comando/petición, esperado, obtenido y evidencia fechada.
- [ ] Con tres registros, un tamaño de página de dos devuelve dos registros en la primera página y uno en la segunda sin duplicarlos. El conteo devuelve tres. Tras borrar uno devuelve dos y el otro motor permanece intacto.

#### HU-027 · Auditar dependencias hexagonales y mappers

**Estado:** Parcial. **Dependencias:** HU-018, HU-021, HU-024. **Trazabilidad:** RA-01. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [ ] Dominio sin dependencias de Spring, controladores o motores concretos.
- [ ] Application usa puertos y los adaptadores los implementan.
- [ ] Se documenta revisión por módulo y defectos corregidos.

#### HU-028 · Consolidar Swagger/OpenAPI y ejemplos de API final

**Estado:** Parcial. **Dependencias:** HU-014, HU-018, HU-021, HU-024. **Trazabilidad:** RT-03. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [ ] Swagger presenta cada endpoint entregado, con DTO/códigos.
- [ ] Se registran ejemplo de solicitud/respuesta por recurso y motor cuando aplica.
- [ ] Contrato y pruebas coinciden con las rutas efectivas.
- [ ] Swagger 3 documenta las seis operaciones de cada recurso, con parámetros del listado parcial y esquema entero para el conteo.

### EP-05 · README, informe y publicación

#### HU-029 · Completar README de instalación y operación reproducible

**Estado:** Parcial. **Dependencias:** HU-005, HU-026, HU-028. **Trazabilidad:** RE-01. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P0.

- [ ] README incluye requisitos/versiones efectivas, DDL/DML, configuración, compilación y arranque REST/CLI.
- [ ] Incluye acceso a Swagger y pruebas reproducibles.
- [ ] Un integrante distinto sigue los pasos desde clon limpio; se registra resultado.

#### HU-030 · Consolidar informe académico y evidencias del procedimiento

**Estado:** Parcial. **Dependencias:** HU-026, HU-027, HU-028. **Trazabilidad:** RE-03. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P0.

- [ ] Informe incluye portada, marco conceptual, diseño, procedimiento, conclusiones, lecciones y referencias.
- [ ] Evidencias distinguen línea base JDK 11 y CRUD final.
- [ ] Se comprueba coherencia entre SRS, SAD, SDD, código y resultados.
- [ ] El informe identifica JDK 11 como entorno de entrega, separa la modernización diferida y enumera las seis operaciones acordadas.
- [ ] Se actualizan rutas, opciones CLI, resultados y referencias al commit según lo efectivamente entregado.

#### HU-031 · Preparar demostración de REST/CLI y dos motores

**Estado:** Apoyo interno pendiente. **Dependencias:** HU-026, HU-029. **Trazabilidad:** RE-03. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P1.

- [ ] Guion reproduce al menos un CRUD por entidad en cada canal/motor según tiempo disponible.
- [ ] Se demuestra selección dinámica y manejo de errores.
- [ ] Existe plan de recuperación por fallos y se registra ensayo.

#### HU-032 · Publicar tag de entrega y verificar repositorio final

**Estado:** Pendiente. **Dependencias:** HU-029, HU-030, HU-026, HU-028. **Trazabilidad:** RE-02. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P0.

- [ ] El fork contiene scripts, código, README e informe requeridos.
- [ ] El tag apunta al commit integrado verificado y su URL funciona.
- [ ] Se registra URL del tag y checklist final; no se etiqueta una entrega sin pruebas.

## Trabajo diferido · Fuera de los hitos de entrega

### EP-02 · Modernización tecnológica

HU-007 a HU-010 conservan el trabajo de modernización para después de la entrega con JDK 11.

#### HU-007 · Definir matriz de versiones objetivo y estrategia de migración

**Estado:** Diferida. **Dependencias:** HU-005. **Trazabilidad:** RT-02. **Milestone:** Diferido. **Épica:** EP-02. **Prioridad:** P1.

- [ ] Se registra tabla inicial/objetivo con justificación y compatibilidades revisadas.
- [ ] Se acuerda Java objetivo con el equipo; referencia docente Java 21/Spring Boot 4, no se presume versión migrada.
- [ ] Se identifican cambios de javax→jakarta u otros solo cuando afecten el código.

#### HU-008 · Actualizar JDK, Spring Boot y POM padre/módulos

**Estado:** Diferida. **Dependencias:** HU-006, HU-007. **Trazabilidad:** RT-02. **Milestone:** Diferido. **Épica:** EP-02. **Prioridad:** P1.

- [ ] Los POM registran versiones acordadas.
- [ ] Compilan los siete módulos sin dependencias incompatibles.
- [ ] Se registran cambios aplicados y errores solucionados con evidencia.

#### HU-009 · Adaptar persistencia MariaDB y MongoDB a las versiones migradas

**Estado:** Diferida. **Dependencias:** HU-008. **Trazabilidad:** RF-04. **Milestone:** Diferido. **Épica:** EP-02. **Prioridad:** P1.

- [ ] Operaciones iniciales de persona persisten/leen en MariaDB real.
- [ ] Operaciones iniciales de persona persisten/leen en MongoDB real.
- [ ] Drivers, anotaciones JPA/Mongo y mappers quedan compatibles, sin acoplar domain al motor.

#### HU-010 · Restaurar REST, CLI y Swagger con el stack actualizado

**Estado:** Diferida. **Dependencias:** HU-008, HU-009. **Trazabilidad:** RF-02. **Milestone:** Diferido. **Épica:** EP-02. **Prioridad:** P1.

- [ ] REST y CLI inician con la versión actualizada.
- [ ] Swagger UI describe los endpoints realmente disponibles.
- [ ] Se documentan rutas, configuración y comandos verificados tras la migración.
