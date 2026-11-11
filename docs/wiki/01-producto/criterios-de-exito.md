# Criterios de éxito de PersonAPP

[← Producto](README.md) · [SRS](01-SRS.md) · [Trazabilidad](trazabilidad.md)

> **Todos los criterios están inicialmente pendientes.** La tabla describe el éxito esperado, no resultados observados. Las HUs son identificadores internos del plan.

| Estado | ID | Criterio verificable | Historias asociadas | Requisitos |
|---|---|---|---|---|
| ☐ | CE-01 | Ejecutar la plantilla original con REST y CLI en JDK 11 | HU-001–HU-005 | RT-01 |
| ☐ | CE-02 | Reproducir y corregir el Mapper con prueba de regresión | HU-006 | RF-08 |
| ☐ | CE-04 | CRUD de Persona con REST/CLI en MariaDB/MongoDB | HU-012–HU-015; HU-026 | RF-01 a RF-04 |
| ☐ | CE-05 | CRUD de Profesión con REST/CLI en MariaDB/MongoDB | HU-016–HU-018; HU-026 | RF-01 a RF-04 |
| ☐ | CE-06 | CRUD de Teléfono y vínculo a persona con ambos clientes/motores | HU-019–HU-021; HU-026 | RF-01, RF-04, RF-06 |
| ☐ | CE-07 | CRUD de Estudios y vínculo persona-profesión con ambos clientes/motores | HU-022–HU-024; HU-026 | RF-01, RF-04, RF-06 |
| ☐ | CE-08 | Alternar MariaDB/MongoDB en la misma ejecución en REST y CLI | HU-025, HU-026 | RF-05 |
| ☐ | CE-09 | Verificar validaciones, errores, mappers y restricciones hexagonales | HU-011, HU-026, HU-027 | RF-06 a RF-08, RA-01/02 |
| ☐ | CE-10 | Swagger/OpenAPI cubre la API implementada sin operaciones ficticias | HU-028 | RT-03 |
| ☐ | CE-11 | Preparación de ambos motores y scripts DDL/DML reproducibles | HU-004, HU-029 | RF-04, RE-02 |
| ☐ | CE-12 | README permite ejecutar el proyecto desde clon limpio | HU-029 | RE-01 |
| ☐ | CE-13 | Informe y evidencias corresponden a ejecuciones reales | HU-030 | RE-03 |
| ☐ | CE-14 | Demostración recorre REST, CLI, ambos motores y operaciones positivas/negativas | HU-026, HU-031 | RF-01 a RF-08 |
| ☐ | CE-15 | Fork y tag final publicados, revisados y accesibles | HU-001, HU-032 | RE-02 |

## Cobertura funcional mínima por verificar

| Recurso | MariaDB REST | MariaDB CLI | MongoDB REST | MongoDB CLI |
|---|---|---|---|---|
| Persona | Pendiente | Pendiente | Pendiente | Pendiente |
| Profesión | Pendiente | Pendiente | Pendiente | Pendiente |
| Teléfono | Pendiente | Pendiente | Pendiente | Pendiente |
| Estudios | Pendiente | Pendiente | Pendiente | Pendiente |

En cada celda comprobar **consultar por identificador, listar todos, contar, crear, editar y eliminar**, más el listado parcial como operación adicional, más datos inválidos, referencias inexistentes y restricciones relevantes. No sustituir pruebas reales por una captura del menú o por métodos declarados en código.

### Cambio dinámico (caso transversal)

1. Iniciar REST y CLI una sola vez.
2. Crear o consultar un registro escogiendo MariaDB; comprobar su resultado en esa base.
3. Seleccionar MongoDB en la siguiente operación **sin reiniciar**; comprobar su resultado allí.
4. Volver a MariaDB durante la misma ejecución y verificar comportamiento esperado.
5. Repetir con ambos clientes y registrar comandos/solicitudes y resultados.

La alternancia no significa replicación automática entre motores.

### Evidencia que se debe registrar por criterio

- **Identificador:** CE-## y HU-###; PR/issue real si existe.
- **Ambiente:** SO, JDK, Spring Boot, MariaDB, MongoDB, commit/tag y fecha.
- **Acción:** comando, petición REST o ruta de menú CLI con datos de entrada.
- **Resultado:** esperado frente a observado; errores, logs o capturas relevantes.
- **Veredicto:** pasa/no pasa, con enlace a la evidencia y defectos restantes.

Estado operativo de las historias: GitHub Projects, no esta página. Resultados medidos: [`04-implementacion/pruebas/`](../04-implementacion/pruebas/README.md).
