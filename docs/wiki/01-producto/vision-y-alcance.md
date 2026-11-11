# Visión y alcance

[← Producto](README.md) · [SRS](01-SRS.md) · [Backlog](../06-backlog/README.md)

## 1. Visión

PersonAPP es una aplicación académica Java/Spring Boot destinada a demostrar **arquitectura hexagonal (Ports & Adapters)**, separación de la lógica de negocio, patrones Service/Repository y sustitución de adaptadores de persistencia. El objetivo es completar un modelo de personas, profesiones, estudios y teléfonos con operaciones CRUD accesibles tanto por HTTP REST como por una interfaz de consola (CLI).

## 2. Objetivo general

Completar, ejecutar, verificar y documentar el laboratorio tomando como punto de partida el **fork de la plantilla del profesor**, con MariaDB y MongoDB seleccionables **durante la ejecución, sin reiniciar ni recompilar**.

## 3. Objetivos específicos

1. Ejecutar las versiones originales de la plantilla en JDK 11, registrando entorno, scripts, compilación, REST y CLI.
2. Reproducir y corregir el problema de Mapper advertido por el profesor, con evidencia de regresión.
3. Completar la entrega con JDK 11.
4. Completar los cuatro dominios con puertos, casos de uso, adaptadores, mapeadores y operaciones CRUD.
5. Demostrar cada recurso desde REST y CLI contra **cada** motor y alternancia sin reinicio.
6. Entregar documentación OpenAPI, instrucciones reproducibles, scripts, informe y tag Git.

## 4. Funcionalidades dentro del alcance

| Recurso | CRUD esperado | Relación o identidad conocida en el modelo relacional base |
|---|---|---|
| Persona | Consultar por identificador, listar todos, contar, crear, editar y eliminar; adicional: listar parcial | `cc` como identificador |
| Profesión | Consultar por identificador, listar todos, contar, crear, editar y eliminar; adicional: listar parcial | `id` como identificador |
| Teléfono | Consultar por identificador, listar todos, contar, crear, editar y eliminar; adicional: listar parcial | `num` como identificador, `duenio` vincula Persona |
| Estudios | Consultar por identificador, listar todos, contar, crear, editar y eliminar; adicional: listar parcial | `(id_prof, cc_per)` vincula Profesión y Persona |

La representación documental en MongoDB deberá definirse y validarse contra los adaptadores, preservando el significado de esas relaciones. No asumir claves o restricciones MongoDB no verificadas.

### Interfaces y persistencia

- **Entradas:** `rest-input-adapter` y `cli-input-adapter`.
- **Núcleo:** `domain` y `application`, con soporte `common`.
- **Salidas:** `maria-output-adapter` y `mongo-output-adapter`.
- **Selección:** el usuario/petición elige `MARIA` o `MONGO` (según las opciones reales de la aplicación) sin cambiar de binario ni reiniciar.

## 5. Etapas obligatorias del trabajo

| Orden | Etapa | Hitos del backlog |
|---|---|---|
| 1 | Fork, diagnóstico, preparación y ejecución original | EP-01 / HU-001 a HU-006 |
| 2 | Completar dominio, REST, CLI y persistencias | EP-03 / HU-011 a HU-024 |
| 3 | Validar CRUD, arquitectura, Swagger y alternancia | EP-04 / HU-025 a HU-028 |
| 4 | Documentar, demostrar y publicar entrega | EP-05 / HU-029 a HU-032 |

Los hitos de entrega se mantienen en el backlog.

## 6. Fuera de alcance

No se presupone: frontend independiente, microservicios, Kafka, PostgreSQL, API Gateway, arquitectura EDA, autenticación completa, despliegue productivo, CI obligatoria, pruebas de carga ni integración entre motores o replicación de datos. Tampoco es un requisito implementar CQRS: seleccionar un motor no constituye CQRS.

## 7. Actores y entregables

**Actores:** usuario REST, usuario CLI, equipo desarrollador y evaluadores académicos.

**Entregables:** código Java multimódulo en el fork; scripts DDL/DML, instrucciones de despliegue, contratos REST, evidencias de pruebas, informe académico, URL de repositorio/tag. La demostración debe enseñar REST y CLI, MariaDB y MongoDB.

## 8. Decisiones aún por validar

- Versiones **finales realmente compatibles y ejecutadas** (la plantilla base usa Java 11 y Spring Boot 2.7.11).
- Rutas HTTP definitivas, DTO, restricciones, formatos y errores precisos.
- Política concreta de selección de motor por operación y tratamiento de entradas inválidas.
- Resultado y localización exacta del defecto de Mapper, solo tras reproducirlo.
- Acceso al fork definitivo, responsables y cronograma real del equipo.

Las dudas se registran en [puntos abiertos](../02-arquitectura/puntos-abiertos.md); no se resuelven inventando comportamientos.
