# 03 · Contratos de PersonAPP

**Producto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de trabajo; contratos finales sujetos a ejecución y aprobación del equipo.  
**Fuente:** [SRS](../01-producto/01-SRS.md) · [SAD](../02-arquitectura/02-SAD-HLD.md) · [Backlog](../06-backlog/README.md)

Esta carpeta define **qué intercambian y qué garantizan** las interfaces y la persistencia. Un método existente en un puerto Java no equivale a un endpoint HTTP publicado ni a una opción CLI funcionando. Lo ya observado se diferencia de lo previsto.

| Documento | Contenido y fuente de evidencia | Historias relacionadas |
|---|---|---|
| [API REST](api-rest.md) | Endpoints confirmados de la base, propuesta de recursos, DTO, respuestas, OpenAPI y registro de cambios | HU-005, HU-010, HU-014, HU-018, HU-021, HU-024, HU-028 |
| [CLI](cli.md) | Opciones, flujos por recurso, menú, selector de motor, mensajes y pruebas | HU-005, HU-010, HU-015, HU-018, HU-021, HU-024, HU-025 |
| [Persistencia](persistencia.md) | MariaDB y MongoDB, identificadores, relaciones, puertos, scripts, aislamiento y cambio dinámico | HU-004, HU-009, HU-011, HU-013, HU-017, HU-020, HU-023, HU-025 |
| [Validaciones y errores](validaciones-y-errores.md) | Validación por recurso y origen, errores REST/CLI, casos negativos y regresión de mappers | HU-006, HU-011, HU-026, HU-027 |
| [backlog único](../06-backlog/README.md) | Historias y criterios de aceptación | HU-025, HU-026, HU-028 |

## Convenciones de lectura

- **Base observada / referencia documental:** información reconocida en los materiales originales. Todavía se debe corroborar en el fork del equipo y registrar commit.
- **Objetivo requerido:** comportamiento esperado según SRS y enunciado, todavía no acreditado.
- **Propuesta de contrato:** convención diseñada para discutir y aprobar, **no** ruta, payload ni política implementada.
- **Verificado:** requiere prueba ejecutada, resultado real, commit y evidencia. No se marca ningún comportamiento como verificado de forma anticipada.

## Orden para cerrar contratos

1. HU-005: ejecutar la plantilla original y capturar rutas/menús efectivos.
2. HU-011: decidir tipos, identidad, reglas transversales y representación de datos; resolver [puntos abiertos](../02-arquitectura/puntos-abiertos.md).
3. HU-014, HU-018, HU-021, HU-024: acordar y publicar rutas y opciones CLI al desarrollar cada recurso.
4. HU-025 y HU-026: comprobar alternancia de motores y las seis operaciones sobre los dos almacenes reales.
5. HU-028: confrontar Swagger/OpenAPI contra aplicación y ejemplos reproducibles.

**Regla de modificación:** cuando cambie una ruta, atributo, validación o clave se actualizan **en el mismo PR** el documento dueño, pruebas, SDD y cualquier ADR/SRS afectado. No se copian contratos de PersonAPI (.NET) ni FoodFlow EDA.
