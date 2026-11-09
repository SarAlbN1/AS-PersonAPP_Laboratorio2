# SRS | Especificación de requisitos de software

**Producto:** PersonAPP · Laboratorio 2 · Arquitectura de Software  
**Estado:** línea base de planificación documental, pendiente de validación contra entrega y pruebas  
**Origen:** enunciado del Laboratorio 2 y criterios de cobertura del equipo.
**Identificadores:** [backlog único](../06-backlog/README.md).

[← Producto](README.md) · [Trazabilidad](trazabilidad.md) · [Criterios de éxito](criterios-de-exito.md)

## 1. Propósito y alcance

Especificar **qué** debe cumplir la solución y cómo se acepta, sin sustituir el SAD (arquitectura) ni el SDD (diseño detallado). La aplicación gestiona Persona, Profesión, Teléfono y Estudios, accede desde REST y CLI, persiste alternativamente en MariaDB o MongoDB y permite elegir el motor **en ejecución, sin reinicio**.

## 2. Fuentes y precedencia

- Enunciado y aclaraciones docentes: obligaciones del laboratorio.
- SRS: requisitos y criterios de aceptación.
- SAD, ADR: decisiones técnicas aprobadas, subordinadas al enunciado.
- Contratos REST/CLI/persistencia: detalle verificable de interfaces.
- Código y pruebas: evidencia de implementación, no sustituto de los requisitos.

## 3. Requisitos funcionales del alcance propuesto

| ID | Requisito verificable | Criterio de aceptación |
|---|---|---|
| RF-01 | Operaciones CRUD para **Persona, Profesión, Teléfono y Estudios**. | Crear, listar todos, listar parcialmente, editar, eliminar y contar en cada recurso con resultados consistentes. |
| RF-02 | Publicar operaciones de los cuatro recursos por REST. | Solicitudes y respuestas HTTP verificadas y acordes con el contrato efectivo. |
| RF-03 | Exponer por CLI las operaciones funcionales requeridas. | Menú ejecutable, entradas/salidas verificadas por recurso y sin operaciones de relleno. |
| RF-04 | Persistir los recursos en **MariaDB y MongoDB** a través de adaptadores. | Escrituras, lecturas y modificaciones observables en el motor seleccionado. |
| RF-05 | Elegir el motor en tiempo de ejecución. | Alternar `MARIA`/`MONGO` por solicitud o desde la CLI **sin reiniciar ni recompilar**. |
| RF-06 | Preservar identidad, vínculos y reglas de validación del dominio. | Rechazar duplicados, datos inválidos y referencias imposibles según los contratos acordados. |
| RF-07 | Manejar ausencias y errores de manera controlada en REST y CLI. | No devolver éxito falso, excepciones sin procesar ni respuestas contradictorias; códigos/mensajes se fijan en contratos. |
| RF-08 | Realizar correctamente los mapeos entre DTO, dominio, entidad JPA y documentos Mongo. | Pruebas de conversión/round-trip y regresión del defecto encontrado. |

**Desglose de RF-01 acordado como alcance del proyecto:** la cobertura CRUD alcanza cuatro entidades × seis operaciones (crear, listar todos, listar parcialmente, editar, eliminar y contar), en dos interfaces y dos motores; HU-026 recoge el criterio de verificación en el [backlog único](../06-backlog/README.md). La disponibilidad de un método Java no acredita que se exponga desde HTTP o CLI.

## 4. Requisitos arquitectónicos

| ID | Requisito | Evidencia de aceptación |
|---|---|---|
| RA-01 | Mantener núcleo de dominio desacoplado de REST, CLI, JPA y MongoDB; respetar dirección de dependencias hexagonales. | Inspección de POM, importaciones y pruebas de arquitectura según el alcance. |
| RA-02 | Aplicar puertos de entrada/salida, adaptadores, casos de uso (Service) y Repository. | Puertos concretos, implementaciones Maria/Mongo y casos de uso trazables. |

Los siete módulos de la plantilla son `common`, `domain`, `application`, `rest-input-adapter`, `cli-input-adapter`, `maria-output-adapter`, `mongo-output-adapter`. **Son módulos Maven, no microservicios.**

## 5. Requisitos técnicos

| ID | Requisito | Evidencia de aceptación |
|---|---|---|
| RT-01 | Usar Java, Spring Boot y Maven multimódulo, partiendo de la plantilla original. | Compilación y arranque de ambas interfaces sobre los módulos originales. |
| RT-02 | Modernización posterior, diferida de la entrega en JDK 11. | Aplicable si el equipo retoma HU-007 a HU-010 después de la entrega. |
| RT-03 | Documentar la API real mediante Swagger/OpenAPI. | Swagger accesible y actualizado con operaciones, parámetros y respuestas implementadas. |

**Stack escrito.** JDK 11, Spring Boot, MariaDB, MongoDB, REST, CLI y Swagger 3. La plantilla declara Spring Boot 2.7.11.

## 6. Requisitos de entrega

| ID | Requisito | Evidencia de aceptación |
|---|---|---|
| RE-01 | README para configurar entorno, bases, scripts, compilar y lanzar REST/CLI. | Un tercero reproduce el procedimiento desde un clon limpio. |
| RE-02 | Código en fork del equipo, scripts DDL/DML y URL del tag final Git. | Archivos versionados, tag publicado y acceso verificable. |
| RE-03 | Informe académico con portada, marco conceptual, diseño, procedimiento, conclusiones, lecciones y referencias. | Documento coherente con código ejecutado y evidencias auténticas. |

El tag exacto `v1.0.0`, si se adopta, es una convención de planificación y debe comprobarse con las instrucciones del profesor.

## 7. Modelo de datos y relaciones de referencia

| Recurso | Identidad del modelo relacional base | Atributos principales | Dependencias |
|---|---|---|---|
| Persona | `cc` | nombre, apellido, genero, edad | Referenciada por Telefono y Estudios |
| Profesión | `id` | nom, des | Referenciada por Estudios |
| Teléfono | `num` | oper, duenio | `duenio` referencia Persona |
| Estudios | `(id_prof, cc_per)` | fecha, univer | Relaciona Profesión y Persona |

**Alcance de esta tabla:** corresponde al esquema relacional de referencia; en MongoDB las claves y representaciones requieren un diseño explícito y pruebas. Evitar introducir identificadores artificiales o reglas distintas sin decisión aprobada.

## 8. Restricciones, supuestos y límites

1. No sustituir MariaDB o MongoDB por un motor único.
2. El selector dinámico debe operar **sin reinicio** y evitar acoplar el dominio al motor concreto.
3. No atribuir funcionalidades al proyecto basándose solo en clases o métodos declarados.
4. No introducir tecnologías de FoodFlow EDA o PersonAPI .NET.
5. No marcar rutas, códigos HTTP, restricciones o versiones como definitivos sin validación de código/contrato.
6. No publicar credenciales ni salidas de excepción que expongan secretos.

## 9. Estrategia de verificación propuesta

La entrega sigue la línea base en JDK 11 (HU-001 a HU-006), el CRUD (HU-011 a HU-024), la verificación (HU-025 a HU-028) y los entregables (HU-029 a HU-032).

HU-026 comprueba las seis operaciones de cada recurso desde REST y CLI sobre ambos motores. Los criterios y las evidencias se vinculan al backlog único.

Para un requisito cumplido se necesita: issue y PR reales, implementación verificable, escenario/comando, resultado esperado y observado, fecha, entorno y evidencia. Ver [trazabilidad](trazabilidad.md).

## 10. Puntos abiertos

- Establecer rutas, cuerpos y códigos de respuesta REST finales.
- Definir opciones, navegación y comportamiento de errores en CLI.
- Confirmar semántica del cambio de base por petición y consistencia de datos entre motores (son almacenes alternativos, no se presupone sincronización).
- Concretar validaciones del dominio, integridad referencial y comportamiento de eliminación.
- Registrar las versiones y resultados de ejecución con JDK 11.
- Registrar fork, responsables, cronograma y tag efectivos.

Consultar y actualizar [puntos-abiertos.md](../02-arquitectura/puntos-abiertos.md) antes de introducir decisiones que cambien el alcance.
