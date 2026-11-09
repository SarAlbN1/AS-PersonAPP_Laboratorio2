# Riesgos, mitigaciones y supuestos

**Proyecto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de diseño basada en enunciado y plantilla de referencia; verificación de implementación pendiente  
[← Arquitectura](README.md) · [SRS](../01-producto/01-SRS.md) · [Backlog](../06-backlog/README.md)


## Registro de riesgos

| ID | Riesgo | Impacto | Mitigación / indicador | HU |
|---|---|---|---|---|
| R-01 | Actualizar JDK/Boot sin baseline reproducible | Alto | Compilar/arrancar plantilla primero y guardar errores originales | HU-003/005 |
| R-02 | Incompatibilidades Boot 2.7 → 4.x (Jakarta, springdoc, JPA, drivers) | Alto | Matriz de compatibilidad, migración por etapas, tests REST/CLI | HU-007 a HU-010 |
| R-03 | Defecto Mapper heredado confunde el diagnóstico de migración | Alto | Reproducir, localizar y crear prueba de regresión antes de cambios mayores | HU-006 |
| R-04 | Scripts Mongo iniciales apuntan a `prueba_db` mientras Spring usa `persona_db` | Alto | Unificar configuración de referencia y comprobar colección real | HU-004 |
| R-05 | Selección de base usa estado global mutable | Alto | Diseño por petición, alternancia y prueba de intercalación | HU-025 |
| R-06 | Documentos Mongo no preservan identidad/relaciones del modelo SQL | Alto | Contrato explícito y pruebas por entidad | HU-011/017/020/023/026 |
| R-07 | Código inicial expone solo parte del CRUD | Alto | Inventario por interfaz y módulo, cierre por pruebas | HU-002/012 a HU-024 |
| R-08 | Swagger desalineado con los controladores entregados | Medio | Verificar documento generado y ejecutar endpoints | HU-028 |
| R-09 | Integración simultánea modifica POM y contratos compartidos | Medio | PR breves, responsables por HU, coordinación y revisión cruzada | HU-001 transversal |
| R-10 | Entrega no reproducible o sin pruebas auténticas | Alto | Runbook desde clon limpio, evidencias y checklist tag | HU-029 a HU-032 |

## Supuestos explícitos, pendientes de comprobación

- S-01: el fork conserva los siete módulos originales y scripts de ambas bases (confirmar HU-002).
- S-02: Java 11 / Spring Boot 2.7.11 son la línea inicial reportada (confirmar POM original).
- S-03: MariaDB y MongoDB pueden ejecutarse localmente o en entorno accesible; no se exige Docker salvo nueva indicación docente.
- S-04: los dos motores son **alternativos**, no réplicas sincronizadas.
- S-05: la definición exacta de rutas REST, códigos, CLI y borrado se fijará al cerrar contratos, no desde conjeturas.
- S-06: el equipo dispone de un fork propio y permisos GitHub para PR y revisión; documentar URL reales al tenerlas.

## Seguimiento

Al materializarse un riesgo, registrar issue/PR, fecha, síntoma reproducido, responsable y mitigación aplicada. Los puntos que requieren decisión formal se gestionan en [puntos abiertos](puntos-abiertos.md) y [ADR](decisiones-adr.md).
