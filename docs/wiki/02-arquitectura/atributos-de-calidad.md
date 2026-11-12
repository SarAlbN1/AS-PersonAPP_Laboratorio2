# Atributos de calidad verificables

**Proyecto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de diseño basada en enunciado y plantilla de referencia; verificación de implementación pendiente  
[← Arquitectura](README.md) · [SRS](../01-producto/01-SRS.md) · [Backlog](../06-backlog/README.md)


Los siguientes criterios son **objetivos de aceptación propuestos**, no mediciones. No se atribuyen umbrales de rendimiento ni disponibilidad que el enunciado no solicite.

| ID | Atributo | Escenario verificable | Criterio de aprobación | HU |
|---|---|---|---|---|
| QA-01 | Modularidad | Inspeccionar árbol Maven y dependencias | Siete módulos originales presentes; dominio no importa adaptadores | HU-002/027 |
| QA-02 | Mantenibilidad | Cambiar una implementación de salida sin alterar dominio | Núcleo usa contratos; JPA/Mongo quedan en adaptadores | HU-027 |
| QA-03 | Correctitud funcional | CRUD de cada recurso en dos clientes y dos motores | Seis operaciones verificadas por recurso, entrada y motor | HU-026 |
| QA-04 | Integridad | Duplicados, claves y referencias inválidas | Se preservan restricciones conforme al contrato de cada motor | HU-011/026 |
| QA-05 | Interoperabilidad | Invocar API desde cliente HTTP independiente | Swagger corresponde a rutas y cuerpos reales | HU-028 |
| QA-06 | Cambiabilidad de persistencia | MARIA → MONGO → MARIA sin detener servicio/CLI | Cada consulta/operación usa el motor indicado, sin contaminación | HU-025 |
| QA-07 | Reproducibilidad | Arranque limpio desde fork + scripts | REST y CLI inician; Maria/Mongo conectan con datos de prueba | HU-003/004/005/029 |
| QA-08 | Compatibilidad | Compilar antes/después de migrar | Versiones exactas registradas y dependencias coherentes | HU-005/007/008/009/010 |
| QA-09 | Testabilidad | Probar caso de uso con puertos sustitutos y salida real | Pruebas unitarias/integración adecuadas y ejecutables | HU-026/027 |
| QA-10 | Seguridad básica | Revisar commits y respuestas de error | Sin secretos en repo ni trazas sensibles en respuestas | HU-029/032 |

## Matriz mínima por recurso

| Recurso | REST+Maria | REST+Mongo | CLI+Maria | CLI+Mongo |
|---|---|---|---|---|
| Persona | CRUD | CRUD | CRUD | CRUD |
| Profesión | CRUD | CRUD | CRUD | CRUD |
| Teléfono | CRUD | CRUD | CRUD | CRUD |
| Estudios | CRUD | CRUD | CRUD | CRUD |

CRUD comprende tres lecturas (consulta por identificador, listado total y conteo) y tres escrituras (alta, edición y eliminación). El listado parcial se verifica como operación adicional. Se añaden pruebas negativas de validación, relación, motor desconocido y conexión indisponible.

## Reglas de evidencia

Una prueba se presenta como completada únicamente con comando/escenario, fecha, entorno, resultado esperado, observado y referencia a commit o PR. Los datos de verificación viven en [`04-implementacion/pruebas/`](../04-implementacion/pruebas/README.md) y la trazabilidad en [Producto](../01-producto/trazabilidad.md).
