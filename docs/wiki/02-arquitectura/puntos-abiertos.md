# Puntos abiertos y decisiones por verificar

**Proyecto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de diseño basada en enunciado y plantilla de referencia; verificación de implementación pendiente  
[← Arquitectura](README.md) · [SRS](../01-producto/01-SRS.md) · [Backlog](../06-backlog/README.md)


Un punto abierto no es una decisión aprobada. El cierre exige **decisión, justificación y evidencia**, no solo una edición de Markdown.

| ID | Pregunta / incertidumbre | Acción de cierre | HU |
|---|---|---|---|
| A-01 | ¿Qué versiones se usaron al ejecutar la entrega con JDK 11? | Registrar versiones, compilación y arranques | HU-003 y HU-005 |
| A-02 | ¿Qué Mapper falla en la plantilla y por qué? | Reproducir error, traza, corrección y regresión | HU-006 |
| A-03 | ¿Se usa `persona_db` o `prueba_db` en Mongo? | Contrastar scripts y propiedades, unificar, probar | HU-004 |
| A-04 | ¿Cuáles son rutas REST, DTO, códigos y errores definitivos? | Contrato acorde al código y Swagger | HU-014/018/021/024/028 |
| A-05 | ¿Cuáles son las operaciones del menú CLI por recurso? | Menús, entradas/salidas y pruebas | HU-015/018/021/024 |
| A-06 | ¿Cómo se preservan claves compuestas, asociaciones y borrado en ambos motores? | Contrato y casos negativos | HU-011/017/020/023/026 |
| A-07 | ¿Dónde se resuelve MARIA/MONGO sin estado global mutable? | Revisar código, decidir estrategia y alternar A-B-A | HU-025 |
| A-08 | ¿Cuáles son URL de fork, responsables, PR y tag? | Incorporar URLs reales en repositorio y entrega | HU-001/032 |
| A-09 | ¿REST y CLI son procesos separados y con qué comandos arrancan? | Registrar procesos/artefactos/puertos reales | HU-005/010/029 |
| A-10 | ¿Hay cambios de alcance obligatorios posteriores al enunciado? | Confirmación docente y actualización SRS/ADR | Transversal |

## Formato de cierre

`ID · decisión · responsable · fecha · fuentes · PR/commit · prueba/evidencia · documentos actualizados`.

No borrar preguntas resueltas: conservar el contexto y marcar `CERRADO` con sus referencias. No fijar un comportamiento futuro en un ADR como "Aprobado" sin revisión del equipo.
