# Comportamiento, errores y selección dinámica

**Proyecto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de diseño basada en enunciado y plantilla de referencia; verificación de implementación pendiente  
[← Arquitectura](README.md) · [SRS](../01-producto/01-SRS.md) · [Backlog](../06-backlog/README.md)


## 1. Ruta funcional común

REST/CLI valida entrada y recibe motor → caso de uso usa puerto de salida → adaptador seleccionado convierte modelo ↔ representación persistente → motor confirma resultado → caso de uso responde → adaptador de entrada traduce a HTTP o consola.

## 2. Comportamiento esperado a especificar y verificar

| Situación | Invariante obligatoria | Respuesta concreta |
|---|---|---|
| Alta válida | Entidad persistida únicamente en motor solicitado | HTTP/CLI: definir en contrato |
| Consulta de identificador no existente | No construir datos ficticios ni reportar éxito | HTTP 404 sugerido, confirmar contrato |
| Identificador duplicado | No sobrescribir sin orden explícita | HTTP 409 sugerido, confirmar contrato |
| Entrada inválida | Validación previa y restricciones de persistencia | HTTP 400 sugerido, confirmar contrato |
| Teléfono sin persona / estudio con referencia ausente | No dejar relaciones inválidas | Validación de negocio y persistencia por motor |
| Eliminación con dependencias | No perder datos relacionados por accidente | Política por definir en `A-06` |
| Motor distinto de `MARIA`/`MONGO` | No escoger silenciosamente una base por defecto | Respuesta explícita documentada |
| MariaDB/MongoDB sin conexión | No devolver éxito falso ni filtrar credenciales | Error controlado, registro técnico seguro |
| Mapper defectuoso | Evitar pérdida/intercambio de campos | Prueba de regresión HU-006 |

**Nota:** los códigos HTTP son una **propuesta de contrato**, no endpoints ni respuestas verificadas. El comportamiento del CLI debe ser semánticamente equivalente, con mensajes de texto.

## 3. Cambio de motor en caliente

**Caso crítico:** A (MARIA) → B (MONGO) → C (MARIA), en solicitudes sucesivas sin reiniciar REST o CLI. La consulta final en MARIA conserva los datos iniciales. No se presupone traslado de registros entre bases ni selección con `spring.profiles.active` cambiada después del arranque.

**Diseño propuesto por validar (ADR-002):** resolver la implementación de un puerto a partir de un valor por petición/acción (`MARIA` / `MONGO`) mediante registro inmutable de estrategias, no mutación de un singleton compartido. Se comprueba en HU-025, incluido escenario de peticiones intercaladas.

## 4. Operaciones y cobertura

Para Persona, Profesión, Teléfono y Estudios: crear, listar todos, listar parcialmente, editar, eliminar y contar, desde REST y CLI, contra MariaDB y MongoDB. HU-026 recoge los criterios y la evidencia por escenario. Distinguir los éxitos de las rutas negativas de integridad.

## 5. Criterio de cierre

Por escenario: registrar petición/opción, base elegida, resultado esperado, resultado observado, fecha y evidencia. Las políticas aún no resueltas se listan en [puntos abiertos](puntos-abiertos.md), no se toman como implementación concluida.
