# 02 · Arquitectura de PersonAPP

Esta carpeta fija el diseño arquitectónico de referencia del laboratorio, separando **lo exigido por el enunciado**, **la solución de partida** y **las decisiones propuestas aún sin validar**. Los estados reales de las historias viven en GitHub Projects, no aquí.

| Documento | Para qué sirve |
|---|---|
| [02-SAD-HLD.md](02-SAD-HLD.md) | Arquitectura, módulos, drivers, vistas y justificación |
| [reglas-arquitectonicas.md](reglas-arquitectonicas.md) | Dependencias permitidas, prohibiciones y revisión de PR |
| [estilo-y-flujo.md](estilo-y-flujo.md) | Vista estática y recorrido REST/CLI |
| [comportamiento-del-flujo.md](comportamiento-del-flujo.md) | Operaciones, excepciones y cambio de motor |
| [decisiones-adr.md](decisiones-adr.md) | Registro y estados de decisiones, enlazado a [ADR](adr/README.md) |
| [atributos-de-calidad.md](atributos-de-calidad.md) | Escenarios y criterios verificables, sin resultados inventados |
| [riesgos-y-supuestos.md](riesgos-y-supuestos.md) | Riesgos, mitigaciones, disparadores y supuestos |
| [puntos-abiertos.md](puntos-abiertos.md) | Decisiones y discrepancias por resolver |
| [diagramas/](diagramas/README.md) | Fuentes editables y reglas de renderizado |

**Fuentes de verdad:** enunciado y aclaraciones docentes → SRS (qué) → SAD y ADR aprobados (cómo se estructura) → contratos (cómo interactúa) → SDD (cómo se codifica). El código y las pruebas aportan la **evidencia de ejecución**; las HU delimitan el trabajo planificado.

**Orden técnico exigido:** baseline original (HU-001 a HU-006), CRUD completo (HU-011 a HU-024), verificación transversal (HU-025 a HU-028), entrega (HU-029 a HU-032).
