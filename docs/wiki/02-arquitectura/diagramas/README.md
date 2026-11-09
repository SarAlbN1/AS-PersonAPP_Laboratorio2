# Diagramas editables · PersonAPP

[← Arquitectura](../README.md) · [SAD/HLD](../02-SAD-HLD.md)

**Fuentes editables:** archivos `.mmd` (Mermaid). No modificar imágenes exportadas como fuente de verdad. Los diagramas expresan el **diseño de referencia**, no el porcentaje de implementación.

| Archivo | Vista | Alcance |
|---|---|---|
| [contexto.mmd](contexto.mmd) | Contexto | Usuarios y motores externos |
| [modulos-hexagonales.mmd](modulos-hexagonales.mmd) | Módulos/colaboración | Dos entradas, núcleo y dos salidas |
| [flujo-rest.mmd](flujo-rest.mmd) | Secuencia | Operación REST, caso de uso y base |
| [seleccion-dinamica.mmd](seleccion-dinamica.mmd) | Secuencia | Alternancia MARIA→MONGO→MARIA |

Se pueden visualizar directamente en Markdown con bloques Mermaid o renderizar con la herramienta Mermaid CLI, si el equipo la instala. No se afirma disponer de PNG exportados.

**Regla de mantenimiento:** si cambia una decisión arquitectónica, actualizar archivo `.mmd`, SAD, ADR y pruebas de la misma HU. La composición interna exacta queda pendiente de validar contra el fork y el SDD.
