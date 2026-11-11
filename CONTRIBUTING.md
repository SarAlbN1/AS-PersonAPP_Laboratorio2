# Cómo contribuir a PersonAPP

Empieza por la [wiki](docs/wiki/Home.md) y consulta el documento que mantiene cada decisión.

| Para | Fuente |
|---|---|
| Confirmar alcance, responsables y dependencias | [Backlog](docs/wiki/06-backlog/README.md) y [gestión de issues](docs/wiki/05-proceso/issues-etiquetas-y-milestones.md) |
| Preparar una rama y registrar cambios | [Ramas y commits](docs/wiki/05-proceso/git-ramas-y-commits.md) |
| Cambiar arquitectura, implementación o interfaces | [Reglas arquitectónicas](docs/wiki/02-arquitectura/reglas-arquitectonicas.md), [SDD](docs/wiki/04-implementacion/03-SDD-LDD.md) y [contratos](docs/wiki/03-contratos/README.md) |
| Validar el trabajo y documentar evidencia | [Pruebas](docs/wiki/04-implementacion/pruebas.md) y [DoR/DoD](docs/wiki/05-proceso/dor-y-dod.md) |
| Abrir, revisar, aprobar o fusionar un PR | [Pull requests y releases](#pull-requests-y-releases) |
| Trabajar con un asistente | [CLAUDE.md](CLAUDE.md) |

Consulta el [README](README.md) para preparar y ejecutar el proyecto.

## Estados del tablero

Actualiza la tarjeta de la issue en el [tablero de PersonAPP](https://github.com/users/SarAlbN1/projects/10) cuando cambie su situación.

- **Backlog**. La historia puede iniciarse y aún no se ha empezado.
- **Blocked**. Una dependencia abierta impide avanzar con la historia. Registra en la issue qué dependencia la bloquea. Cuando se resuelva la última, pasa la tarjeta a **Backlog** si el trabajo aún no comenzó o al estado que corresponda según el avance real.
- **In progress**. Mueve la tarjeta al comenzar el trabajo efectivo. Un PR en borrador mantiene este estado si el trabajo puede seguir.
- **In review**. Mueve la tarjeta cuando haya un PR abierto, listo para revisión y vinculado a la entrega de la historia. Un PR parcial que solo referencia la issue no cambia el estado de la historia completa.
- **Done**. Mueve la tarjeta cuando el cambio esté integrado, se hayan verificado los criterios y la issue esté cerrada.

## Pull requests y releases

Esta página es la fuente de las reglas de revisión, aprobación y fusión de PersonAPP. Describe el proceso del equipo. La configuración efectiva de protección de `main` en GitHub queda por verificar.

### Abrir y revisar un PR

1. Integra los cambios a `main` mediante PR desde una rama propia. Usa el flujo de [ramas y commits](docs/wiki/05-proceso/git-ramas-y-commits.md).
2. Describe el cambio, sus criterios y las comprobaciones ejecutadas. Indica los resultados y pendientes. Enlaza la issue cuando exista y usa `Closes #<número>` solo para la issue real que cierre el PR.
3. Solicita revisión de otro integrante con permiso de escritura. El autor no aprueba ni fusiona su propio PR.
4. La revisión comprueba el último commit del PR, los criterios aplicables y la evidencia. Los hallazgos que impidan la integración se resuelven antes de fusionar. Una revisión que solicita cambios debe quedar resuelta por quien la emitió.
5. Un commit nuevo exige volver a verificar el PR y obtener una aprobación vigente para ese commit. El estado fusionable de GitHub por sí solo no satisface la revisión.

### Revisión delegada a un asistente

- Un asistente puede analizar un PR y comunicar hallazgos. Para publicar una revisión, registrar una aprobación o fusionar en GitHub, debe tener autorización expresa para esa acción y ese PR.
- La cuenta activa debe pertenecer a la persona que delega la acción. Esa persona debe ser un integrante con permiso de escritura distinto del autor del PR. El asistente que escribió o modificó el PR no puede aprobarlo ni fusionarlo bajo otra identidad.
- Para **aprobar**, comprueba la identidad del colaborador, el autor y el último commit. Verifica el cambio, registra en GitHub el alcance y los resultados de la revisión y emite la aprobación si se cumplen los criterios y se resolvieron los hallazgos bloqueantes.
- Para **fusionar**, verifica que la autorización incluya esa acción, que el commit actual coincida con el aprobado y que las comprobaciones exigidas estén correctas. Si cambió el PR o fallan los permisos, reporta el bloqueo y detén la operación.

### Fusión y publicación

- Integra mediante **squash** con aprobación vigente de otro integrante, controles requeridos aprobados, conversaciones resueltas y sin solicitudes de cambio activas. Comprueba el commit antes de ejecutar la fusión.
- Respeta las protecciones de GitHub. Las acciones administrativas, los cambios de reglas, la fusión automática, el force push y el borrado de `main` quedan fuera del flujo de integración.
- Después de fusionar, registra la evidencia y actualiza el estado conforme al [DoR/DoD](docs/wiki/05-proceso/dor-y-dod.md). Si se usa un tablero, comprueba qué dependencias quedaron desbloqueadas.
- Publica el tag de entrega sobre el commit integrado y verificado, una vez cumplida la [lista de entrega](docs/wiki/05-proceso/entrega.md) y autorizada la publicación.
