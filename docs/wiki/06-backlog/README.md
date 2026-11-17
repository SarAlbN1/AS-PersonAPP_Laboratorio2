# Backlog único del Laboratorio 2

La entrega comprende un servicio web hexagonal con Repository y Service, línea base JDK 11 y actualización tecnológica validada, Spring Boot, MariaDB, MongoDB, REST, CLI y Swagger 3. El repositorio etiquetado debe incluir el código fuente, scripts DDL/DML y un README reproducible. El informe incluye portada, marco conceptual, diseño, procedimiento, conclusiones y lecciones aprendidas, y referencias.

El equipo definió seis operaciones para Persona, Profesión, Teléfono y Estudios: **crear, consultar por identificador, listar todos, editar, eliminar y contar**. Como funcionalidad adicional, se conserva el listado parcial con paginación y orden estable. El conteo devuelve el total del motor elegido. La selección dinámica de motor es un criterio del equipo.

**Punto de partida.** Persona ya tiene puertos, caso de uso, repositorios y adaptadores para ambos motores. REST declara un listado y un alta cuyo mapper está incompleto. CLI permite listar. El conteo existe como método interno. Profesión, Teléfono y Estudios ya tienen modelos, representaciones de persistencia y mappers. Sus operaciones por REST y CLI siguen pendientes.

El trabajo sobre el fork sigue las cuatro fases del laboratorio:

1. Hacer funcionar la plantilla original y corregir los defectos de mapeo, anotaciones, librerías y dependencias que impidan ejecutarla (HU-001 a HU-006).
2. Actualizar las tecnologías y documentar cómo se hizo la migración (HU-007).
3. Contenerizar la aplicación actualizada y verificar sus funciones disponibles en ambos motores (HU-034).
4. Completar las cuatro entidades, sus tres lecturas y tres escrituras y la consulta Persona–Teléfonos. Validar el resultado en contenedores y cerrar la entrega (HU-011 a HU-033).

Cada fase se valida antes de iniciar la implementación de la siguiente.

Los estados reflejan la inspección del repositorio. Para cerrar una historia, registrar el resultado observado, el comando o solicitud y el commit correspondiente. Aplicar la [definición de terminado](../05-proceso/dor-y-dod.md).

## Restricciones transversales de todas las HU

- Se conservan los siete módulos Maven: `common`, `domain`, `application`, `rest-input-adapter`, `cli-input-adapter`, `maria-output-adapter` y `mongo-output-adapter`. No se añaden, eliminan, fusionan ni renombran módulos ni se trasladan sus responsabilidades.
- Se conserva la arquitectura hexagonal, sus capas y la dirección de dependencias. REST y CLI llaman a puertos/casos de uso; MariaDB y MongoDB implementan puertos de salida. El dominio no depende de frameworks ni infraestructura.
- La actualización de tecnologías, corrección de librerías y contenedorización se realizan dentro de esa estructura. No convierten módulos en microservicios ni introducen brokers, bases adicionales o nuevas capas arquitectónicas.
- Cada HU registra una revisión de estas restricciones. HU-027 verifica la estructura completa y bloquea el cierre de la entrega si hay infracciones. Un ADR no autoriza por sí solo incumplir estas restricciones.

## Hitos de entrega

Un **milestone** agrupa historias por resultado verificable. Una **épica** agrupa historias por objetivo de trabajo. Cada HU de la entrega pertenece a un hito y a una épica. Los IDs son locales. La creación de milestones e issues en GitHub se gestiona aparte.

| Milestone | Épicas | Depende de | Criterio de salida |
|---|---|---|---|
| M1 · Línea base reproducible | EP-01 | — | Plantilla reparada, migración tecnológica validada y aplicación actualizada ejecutada en contenedores con ambos motores. |
| M2 · Operaciones de los cuatro recursos | EP-03 | M1 | Seis operaciones por recurso, paginación adicional y consulta Persona–Teléfonos en REST y CLI, con persistencia en ambos motores. |
| M3 · Verificación y entrega | EP-04 y EP-05 | M2 | Despliegue en contenedores verificado, cobertura completa con evidencias, Swagger 3, README reproducible, informe y URL del tag. |

**Prioridad.** P0 identifica el camino de entrega. P1 identifica apoyo interno. El informe y la auditoría pueden avanzar en paralelo con la implementación. Su cierre depende de la evidencia de las historias previas. El equipo asignará responsables antes de iniciar cada HU.

## M1 · Línea base reproducible

### EP-01 · Repositorio, entorno y mapeos

#### HU-001 · Crear fork del repositorio y establecer flujo de colaboración

**Estado:** Cerrada (issue #1, PR #30). **Dependencias:** Ninguna. **Trazabilidad:** RE-02. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] Existe fork del proyecto docente y URL verificable.
- [x] README identifica el repositorio base y el fork sin confundirlos.
- [x] Se prueba el flujo rama → PR de revisión cruzada; no se modificó el original.

#### HU-002 · Inventariar módulos, dependencias y capacidades de la plantilla

**Estado:** Cerrada (issue #2, PR #31). **Dependencias:** HU-001. **Trazabilidad:** RA-01. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] Se registran siete módulos Maven y sus dependencias.
- [x] Se distingue entre operaciones declaradas, implementadas y expuestas por REST/CLI.
- [x] Se registran rutas, menús y clases observadas con referencias al código.

#### HU-003 · Preparar y documentar ambiente Java y bases de datos originales

**Estado:** Cerrada (issue #3, PR #33). **Dependencias:** HU-001. **Trazabilidad:** RT-01. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] Se registran java -version, mvn -version y versiones reales de MariaDB/MongoDB.
- [x] Se configuran variables/credenciales locales sin subir secretos.
- [x] Se documenta el procedimiento desde un clon limpio.

#### HU-004 · Ejecutar DDL/DML inicial en MariaDB y MongoDB

**Estado:** Cerrada (issue #4, PR #34). **Dependencias:** HU-003. **Trazabilidad:** RE-02. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] DDL/DML o scripts equivalentes se ejecutan y se registran resultados.
- [x] Se detecta y resuelve documentadamente discrepancia persona_db/prueba_db en MongoDB.
- [x] Se comprueban tablas/colecciones y registros de ejemplo.

#### HU-005 · Compilar y ejecutar REST y CLI de la plantilla original

**Estado:** Cerrada (issue #5, PR #35). **Dependencias:** HU-002, HU-004. **Trazabilidad:** RT-01. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] mvn clean install termina o se documentan los bloqueos reproducibles.
- [x] Arranca PersonAppRestApi y se comprueban endpoints iniciales y Swagger.
- [x] Arranca PersonAppCli y se comprueba menú/listado inicial.
- [x] Se guardan comandos, resultados y capturas/logs reales.

#### HU-006 · Hacer funcionar la plantilla y corregir mappers, anotaciones y dependencias

**Estado:** Cerrada (issue #6, PR #38). **Dependencias:** HU-005. **Trazabilidad:** RF-08. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] La falla se reproduce y se registra entrada, salida y error.
- [x] Se corrigen únicamente los defectos reproducidos de mapeo, anotaciones, librerías o dependencias que impiden ejecutar la plantilla, manteniendo los siete módulos, puertos y adaptadores. REST y CLI arrancan y se verifica el flujo reparado.
- [x] Una prueba de regresión demuestra transformación correcta y no se afecta el flujo base.

#### HU-007 · Actualizar Java, Spring Boot, librerías y plugins compatibles

**Issue:** #40. **Responsable:** @SarAlbN1. **Estado:** Cerrada (issue #40, PR #46; el procedimiento quedó en el PR #45). **Dependencias:** HU-002, HU-005, HU-006. **Trazabilidad:** RT-02, RF-08, RA-01, RA-02. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] Registrar las versiones iniciales y adoptar la versión LTS más alta disponible y compatible para Java y cada tecnología que publique versiones LTS. Para tecnologías sin etiqueta LTS, elegir la versión estable más reciente compatible y con soporte vigente. Citar las fuentes oficiales y justificar cualquier versión inferior con la incompatibilidad comprobada.
- [x] Comparar el árbol de dependencias directas y transitivas antes y después. Revisar POM padre e hijos, plugins, librerías, anotaciones, APIs y configuración, y resolver incompatibilidades.
- [x] Compilar todos los módulos, arrancar REST y CLI y comprobar acceso a ambos motores con las versiones finales; adjuntar comandos y resultados.
- [x] Ejecutar pruebas de regresión de mapeos que preserven identificadores, atributos y relaciones, y documentar los cambios de APIs/configuración.
- [x] Conservar los siete módulos, sus responsabilidades y las dependencias hexagonales; documentar en [Versiones](../04-implementacion/versiones.md) el procedimiento reproducible del upgrade con versiones iniciales y finales, orden de pasos, comandos, cambios de dependencias y API, errores encontrados, soluciones y resultados de validación. Enlazarlo desde el README.

#### HU-034 · Contenerizar aplicación y ambos motores conservando los módulos

**Issue:** #41. **Responsable:** @ma-sarmiento. **Estado:** Cerrada (issue #41, PR #49, integrado como `2e0a05d`, y PR #52). **Dependencias:** HU-007, HU-004. **Trazabilidad:** RT-04, RE-01, RA-01, RA-02. **Milestone:** M1. **Épica:** EP-01. **Prioridad:** P0.

- [x] Versionar Dockerfile(s) reproducibles para las entradas REST y CLI, construyendo el reactor de siete módulos sin cambiar su estructura ni responsabilidades.
- [x] Versionar Docker Compose para REST, CLI interactiva y MariaDB/MongoDB, con versiones compatibles con HU-007, conectividad y comprobaciones de disponibilidad.
- [x] Externalizar configuración y credenciales; definir volúmenes persistentes y ejecución documentada de DDL/DML sin borrar datos durante reinicios ordinarios.
- [x] Desde un clon limpio construir imágenes, levantar los servicios, ejecutar CLI, acceder a REST/Swagger y verificar las operaciones disponibles en la plantilla reparada con ambos motores y alternar sin reiniciar la aplicación. La cobertura funcional completa se verifica en HU-026.
- [x] Verificar persistencia después de recrear contenedores conservando volúmenes; registrar comandos, resultados y procedimiento de parada/limpieza.
- [x] Conservar arquitectura modular y hexagonal: los contenedores empaquetan las entradas y bases existentes, sin convertir cada módulo Maven en un servicio.

## M2 · Operaciones de los cuatro recursos

### EP-03 · Contratos y operaciones de dominio

**Persona y contrato común**

#### HU-011 · Definir contratos de dominio, identificadores y validaciones comunes

**Estado:** Cerrada (issue #7, PR #36). **Dependencias:** HU-002, HU-005. **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] Se documentan llaves de persona, profesión, teléfono y estudios (compuesta).
- [x] Se define comportamiento para duplicados, FKs, datos inválidos y eliminaciones.
- [x] REST/CLI y ambos motores acuerdan las mismas reglas funcionales; no se inventan rutas como hechos existentes.
- [x] Se definen listado total, paginación con orden estable y conteo por motor, además de las reglas de identidad para editar y eliminar.

#### HU-012 · Completar puertos y casos de uso CRUD de Persona

**Estado:** Cerrada (issue #8, PR #51). **Dependencias:** HU-011, HU-034. **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] Las seis operaciones funcionan por puertos.
- [x] El caso de uso no importa entidades de MariaDB/MongoDB.
- [x] Se prueban errores y transformaciones relevantes.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-013 · Completar adaptadores de persistencia de Persona en ambas bases

**Estado:** Cerrada (issue #9, PR #56). **Dependencias:** HU-012. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] CRUD de persona opera con MariaDB y MongoDB reales.
- [x] Los mappers preservan identificador y campos.
- [x] Las pruebas no usan un motor simulado como única evidencia.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-014 · Exponer CRUD de Persona por REST y documentarlo

**Estado:** Cerrada (issue #10, PR #64; Swagger en el PR #66). **Dependencias:** HU-013. **Trazabilidad:** RF-02. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] REST ofrece alta, consulta por identificador, listado total, edición, eliminación y conteo, además del listado parcial paginado.
- [x] Estados HTTP, DTO y errores documentados y probados en Swagger/OpenAPI.
- [x] Se verifica selección de motor por petición según contrato definido.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-015 · Exponer CRUD de Persona por CLI

**Estado:** Cerrada (issue #11, PR #65). **Dependencias:** HU-013. **Trazabilidad:** RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] El menú ejecuta alta, consulta por identificador, listado total, edición, eliminación y conteo, además del listado parcial paginado.
- [x] Mensajes de éxito/error identificables y validación de entrada.
- [x] Se ejecuta con cada base sin reiniciar cuando se cambia la opción.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

**Profesión, Teléfono y Estudios**

#### HU-016 · Implementar dominio, puertos y casos de uso de Profesión

**Estado:** Cerrada (issue #12, PR #53). **Dependencias:** HU-011, HU-034 (#41). **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] Modelo y puertos de profesión implementados.
- [x] Casos de uso CRUD y validaciones probados.
- [x] Sin referencias a controladores ni clases de persistencia en dominio.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-017 · Implementar persistencia de Profesión en MariaDB y MongoDB

**Estado:** Cerrada (issue #13, PR #54). **Dependencias:** HU-016. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] Entidades/documentos, repositorios y mappers disponibles.
- [x] CRUD verificado en MariaDB y MongoDB reales.
- [x] Pruebas contemplan clave duplicada y relaciones pertinentes.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-018 · Exponer CRUD de Profesión por REST y CLI

**Estado:** Cerrada (issue #14, PR #55). **Dependencias:** HU-017. **Trazabilidad:** RF-02, RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] REST permite CRUD con contrato documentado.
- [x] CLI permite CRUD de profesión.
- [x] Ambos funcionan con MariaDB y MongoDB y registran casos negativos.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-019 · Implementar dominio, puertos y casos de uso de Teléfono

**Estado:** Cerrada (issue #15, PR #57). **Dependencias:** HU-011, HU-034 (#41). **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] Modelo, identificador y referencia a persona validados.
- [x] Puertos y casos de uso realizan CRUD.
- [x] Se prueba rechazo de dueño inexistente y conflictos de identificador según reglas acordadas.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-020 · Implementar persistencia de Teléfono en ambos motores

**Estado:** Cerrada (issue #16, PR #60). **Dependencias:** HU-019, HU-013. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] CRUD funciona sobre tablas/colecciones reales.
- [x] Relación dueño-persona conserva integridad según cada motor.
- [x] Pruebas verifican lectura posterior y restricciones.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-021 · Exponer CRUD de Teléfono por REST y CLI

**Estado:** Cerrada (issue #17, PR #62). **Dependencias:** HU-020. **Trazabilidad:** RF-02, RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] REST y CLI ejecutan CRUD completo.
- [x] Se documentan respuestas y errores de dueño inexistente.
- [x] Se demuestra en MariaDB y MongoDB.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-022 · Implementar dominio, puertos y casos de uso de Estudios

**Estado:** Cerrada (issue #18, PR #58). **Dependencias:** HU-011, HU-034 (#41). **Trazabilidad:** RF-01. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] Se usa identidad compuesta (id_prof, cc_per) del modelo base, salvo decisión documentada.
- [x] Casos de uso CRUD e integridad de dos referencias.
- [x] Pruebas cubren duplicados y referencias inválidas.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-023 · Implementar persistencia de Estudios en ambos motores

**Estado:** Cerrada (issue #19, PR #59). **Dependencias:** HU-022, HU-013, HU-017. **Trazabilidad:** RF-04. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] CRUD persiste en ambos motores reales.
- [x] El Mapper respeta el par identificador y sus referencias.
- [x] Se prueba persistencia y validación de dependencias.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-024 · Exponer CRUD de Estudios por REST y CLI

**Estado:** Cerrada (issue #20, PR #67). **Dependencias:** HU-023. **Trazabilidad:** RF-02, RF-03. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] API y menú CLI permiten los CRUD previstos.
- [x] El contrato representa los dos campos de la clave compuesta.
- [x] Funcionamiento comprobado en ambos motores y con errores.
- [x] Se verifican las seis operaciones en la capa que cubre la historia: crear, consultar por identificador, listar todos, editar, eliminar y contar; adicionalmente, listar parcialmente. Listar parcial aplica el orden y límite acordados. Contar devuelve el total del motor seleccionado, incluido cero para una colección vacía.

#### HU-033 · Consultar una persona con todos sus teléfonos asociados

**Issue:** #42. **Responsable:** @Juandi48. **Estado:** Cerrada (issue #42, PR #61, #68 y #70; Swagger en el PR #81). **Dependencias:** HU-013, HU-014, HU-015, HU-020, HU-021. **Trazabilidad:** RF-09, RF-02, RF-03, RF-04, RF-07. **Milestone:** M2. **Épica:** EP-03. **Prioridad:** P0.

- [x] Definir e implementar consulta por identificador que entregue la persona y todos sus teléfonos asociados desde REST y CLI, mediante puertos y casos de uso existentes.
- [x] Devolver una colección vacía cuando la persona exista sin teléfonos; definir y probar error controlado para persona inexistente.
- [x] Probar personas con cero, uno y varios teléfonos; excluir teléfonos de otras personas y preservar asociaciones en MariaDB y MongoDB reales.
- [x] Permitir selección dinámica del motor y documentar contrato REST, opciones CLI y ejemplos en Swagger.
- [x] Registrar evidencia de ambos canales y motores; esta consulta no sustituye ninguna de las seis operaciones básicas.

## M3 · Verificación y entrega

### EP-04 · Integración, arquitectura y Swagger

#### HU-025 · Verificar selección dinámica por petición en REST y por menú en CLI

**Estado:** Cerrada (issue #21, PR #72, #75 y #76). **Dependencias:** HU-014, HU-015, HU-018, HU-021, HU-024. **Trazabilidad:** RF-05. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [x] Durante un único proceso REST, solicitudes sucesivas eligen MariaDB y MongoDB.
- [x] Durante una única sesión CLI se alternan ambos motores.
- [x] Cada consulta encuentra sus datos en el motor correspondiente y no cambia globalmente el motor de solicitudes concurrentes.

#### HU-026 · Verificar las seis operaciones desde REST y CLI en ambos motores

**Estado:** Cerrada (issue #22, PR #78); verificada el 2026-11-17 sobre `92b1454`; [evidencia](../04-implementacion/pruebas/README.md#verificación-final-de-las-seis-operaciones-hu-026). **Dependencias:** HU-018, HU-021, HU-024, HU-025, HU-033, HU-034. **Trazabilidad:** RF-01, RF-02, RF-03, RF-04. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [x] Ejecutar las seis operaciones por recurso desde REST y CLI sobre MariaDB y MongoDB. Adjuntar evidencia reproducible de cada flujo.
- [x] Verificar 4 entidades × 6 operaciones × 2 interfaces × 2 motores (96 escenarios básicos), más paginación y consulta Persona–Teléfonos en ambos canales/motores. La consulta por identificador usa la clave completa, compuesta en Estudios.
- [x] Se prueban casos negativos: invalidez, duplicados, relaciones, inexistentes.
- [x] Repetir la aceptación sobre el despliegue en contenedores con las versiones finales de HU-007 y registrar los resultados.
- [x] Cada resultado tiene comando/petición, esperado, obtenido y evidencia fechada.
- [x] Con tres registros, un tamaño de página de dos devuelve dos registros en la primera página y uno en la segunda sin duplicarlos. El conteo devuelve tres. Tras borrar uno devuelve dos y el otro motor permanece intacto.

#### HU-027 · Auditar dependencias hexagonales y mappers

**Estado:** Cerrada (issue #23, PR #73). **Dependencias:** HU-018, HU-021, HU-024. **Trazabilidad:** RA-01. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [x] Dominio sin dependencias de Spring, controladores o motores concretos.
- [x] Application usa puertos y los adaptadores los implementan.
- [x] Se documenta revisión por módulo y defectos corregidos.
- [x] Comparar con la plantilla y verificar los siete módulos originales, sus nombres, responsabilidades y dirección de dependencias; no aceptar adiciones, eliminaciones, fusiones ni redistribuciones arquitectónicas.

#### HU-028 · Consolidar Swagger/OpenAPI y ejemplos de API final

**Estado:** Cerrada (issue #24, PR #66, #80 y #81). **Dependencias:** HU-014, HU-018, HU-021, HU-024, HU-033. **Trazabilidad:** RT-03. **Milestone:** M3. **Épica:** EP-04. **Prioridad:** P0.

- [x] Swagger presenta cada endpoint entregado, con DTO/códigos.
- [x] Se registran ejemplo de solicitud/respuesta por recurso y motor cuando aplica.
- [x] Contrato y pruebas coinciden con las rutas efectivas e incluyen consulta por identificador para cada entidad y consulta Persona–Teléfonos.
- [x] Swagger 3 documenta las seis operaciones de cada recurso, con parámetros del listado parcial y esquema entero para el conteo.

### EP-05 · README, informe y publicación

#### HU-029 · Completar README de instalación y operación reproducible

**Estado:** Cerrada (issue #25, PR #74). **Dependencias:** HU-005, HU-007, HU-026, HU-028, HU-034. **Trazabilidad:** RE-01. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P0.

- [x] El README documenta los requisitos y las versiones verificadas, la configuración del ambiente, la ejecución de los scripts DDL/DML y los pasos de compilación, despliegue y arranque de REST y CLI.
- [x] El README indica cómo acceder a Swagger y cómo reproducir las comprobaciones de ambas entradas.
- [x] Desde un clon limpio, otro integrante sigue el README para preparar el ambiente, compilar, desplegar y arrancar REST y CLI. Registra los comandos y el resultado de cada paso.

#### HU-030 · Consolidar informe académico y evidencias del procedimiento

**Estado:** Cerrada (#26, PR #89). El informe registra el tag `v1.0.0` sobre `632fe23`. **Dependencias:** HU-026, HU-027, HU-028. **Trazabilidad:** RE-03. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P0.

- [x] Informe incluye portada, marco conceptual, diseño, procedimiento, conclusiones, lecciones y referencias.
- [x] Evidencias distinguen línea base JDK 11, versiones finales actualizadas y funcionalidades finales.
- [x] Se comprueba coherencia entre SRS, SAD, SDD, código y resultados.
- [x] El informe identifica JDK 11 como línea base, registra las versiones finales validadas y enumera las seis operaciones, la paginación adicional y la consulta Persona–Teléfonos.
- [x] Se actualizan rutas, opciones CLI, resultados y referencias al commit según lo efectivamente entregado.

#### HU-031 · Preparar demostración de REST/CLI y dos motores

**Estado:** En revisión: el guion está en `main` (PR #77 y #83) y falta el ensayo cronometrado de los tres. **Dependencias:** HU-026, HU-029. **Trazabilidad:** RE-03. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P1.

- [x] Guion reproduce al menos un CRUD por entidad en cada canal/motor según tiempo disponible.
- [x] Se demuestra selección dinámica y manejo de errores.
- [ ] Existe plan de recuperación por fallos y se registra ensayo.

#### HU-032 · Publicar tag de entrega y verificar repositorio final

**Estado:** Pendiente. Falta la lista de entrega verificada y el tag. **Dependencias:** HU-029, HU-030, HU-026, HU-027, HU-028, HU-034. **Trazabilidad:** RE-02. **Milestone:** M3. **Épica:** EP-05. **Prioridad:** P0.

- [ ] El fork contiene scripts, código, README e informe requeridos.
- [ ] El tag apunta al commit integrado verificado y su URL funciona.
- [ ] Se registra URL del tag y checklist final; no se etiqueta una entrega sin pruebas.
