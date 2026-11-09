# Registro de decisiones arquitectónicas (ADR)

**Proyecto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de diseño basada en enunciado y plantilla de referencia; verificación de implementación pendiente  
[← Arquitectura](README.md) · [SRS](../01-producto/01-SRS.md) · [Backlog](../06-backlog/README.md)


**Regla de estados:** `Base del enunciado` significa requisito, no ADR nuevo; `Propuesto` no autoriza implementar una opción concreta; `Aprobado` necesita revisión del equipo y registro de fecha/PR; `Verificado` agrega evidencia de funcionamiento.

| Identificador | Tema | Estado | Detalle / requisito |
|---|---|---|---|
| BASE-01 | Arquitectura hexagonal, Service/Repository, REST/CLI y MariaDB/MongoDB | Base del enunciado | [SAD](02-SAD-HLD.md), RA-01/02 |
| BASE-02 | Elección de motor en ejecución sin reiniciar | Base del enunciado | RF-05 y HU-025 |
| ADR-001 | Actualización gradual de Java, Spring Boot y dependencias | Diferido para después de la entrega en JDK 11 | [ADR-001](adr/ADR-001-estrategia-migracion.md) |
| ADR-002 | Selección de adaptador por operación sin estado mutable global | Propuesto | [ADR-002](adr/ADR-002-seleccion-dinamica.md) |
| ADR-003 | Política de identidad y asociaciones MariaDB/MongoDB | Pendiente de definir | [ADR-003](adr/ADR-003-modelo-persistencia.md) |

## Cambios que requieren ADR o actualización del SAD

Modificación de límites de módulos, estrategia de selección de motor, representación de entidades/relaciones, herramienta de persistencia o versión mayor de framework. Un ajuste local de nombres privados no requiere ADR.

## Plantilla y proceso

[Guía ADR](adr/README.md) · [Plantilla ADR](adr/ADR-000-plantilla.md). El PR de la HU debe actualizar el ADR, contratos y pruebas afectados. Consultar también [puntos abiertos](puntos-abiertos.md).
