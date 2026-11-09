# Preparación de GitHub Projects e importación

[← Backlog](README.md)

## Modelo recomendado

**Proyecto sugerido:** `PersonAPP | Laboratorio 2` (nombre sujeto a decisión del equipo). El tablero puede contener las HU que el equipo acuerde publicar.

| Campo | Tipo sugerido | Valores |
|---|---|---|
| Status | Single select | Backlog, In Progress, In Review, Blocked, Done |
| Priority | Single select | P0, P1 |
| Epic | Single select | EP-01 a EP-05 |
| Milestone | Campo nativo de issue | M1, M2, M3. EP-02 queda sin milestone de entrega. |
| Story Points | Number | estimaciones iniciales |
| Assignee | Campo nativo de issue | cuentas reales por confirmar |
| Dependencies | Texto o relaciones entre issues | IDs HU hasta crear issues reales |

## Antes de cargar

1. Confirmar URL del **fork** del equipo y permisos de escritura, sin inventar propietario o usuario GitHub.
2. Registrar la épica en el campo Epic o mediante un issue padre. Usar los IDs EP del backlog.
3. Si el equipo publica el plan, usar los milestones M1, M2 y M3 del backlog. Asignar a cada issue el hito de su historia.
4. Crear etiquetas `type:feature`, `type:docs`, `type:test`, `priority:P0`, `priority:P1`, `area:core`, `area:rest`, `area:cli`, `area:maria`, `area:mongo`, `area:repo` según necesidad. Evitar etiquetas duplicadas de Status.
5. Abrir issues a partir de [backlog único](README.md), conservando sus criterios completos; asignar cada issue real al Project.

## Añadir trabajo al tablero

Si el equipo usa GitHub Projects, copiar la sección de cada historia que se vaya a gestionar, incluidos sus criterios de aceptación. Al añadir la issue al tablero, conservar el identificador HU en el título. El backlog local permanece en su [único archivo Markdown](README.md).

## Política de estado

- Backlog → In Progress: comienza trabajo real de la issue.
- In Progress → In Review: PR abierto y revisable.
- In Review → Done: PR aprobado e integrado, criterios verificados.
- Blocked: dependencia real que impide avanzar; documentar el bloqueo en la issue.

Cuando exista un Project, registrar ahí el avance de las issues añadidas al tablero.
