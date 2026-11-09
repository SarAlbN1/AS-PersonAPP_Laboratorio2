# CLAUDE.md · Reglas de trabajo para PersonAPP

La [wiki](docs/wiki/Home.md) contiene el diseño y el proceso de PersonAPP. Lee [CONTRIBUTING](CONTRIBUTING.md) antes de modificar archivos. Las reglas de revisión cruzada, aprobación y fusión, incluida la actuación de un asistente en nombre de otro colaborador, se definen en la [política de integración](CONTRIBUTING.md#pull-requests-y-releases).

## Jerarquía

1. El **enunciado del laboratorio** fija las obligaciones académicas. Si falta una fuente que confirme un requisito, registra la incertidumbre y solicita esa fuente.
2. La **wiki** define los requisitos, las decisiones de arquitectura, el diseño y los contratos. El [backlog](docs/wiki/06-backlog/README.md) organiza el trabajo a partir de esas fuentes.
3. Este archivo aplica las reglas del equipo al trabajo con asistentes. Ante discrepancias, señala la fuente afectada y propone la corrección en el lugar donde se mantiene.
4. El **informe académico** presenta el trabajo. Contrasta sus afirmaciones con el enunciado, la wiki y los resultados obtenidos. Coordina las correcciones con quien mantiene el informe.

El código, los commits y las pruebas ejecutadas son la evidencia del estado real. Registra como pendiente lo que falte comprobar.

## Lectura según la tarea

| Tarea | Fuentes |
|---|---|
| Alcance y trabajo asignado | [SRS](docs/wiki/01-producto/01-SRS.md), [backlog](docs/wiki/06-backlog/README.md) y, cuando exista, la issue vinculada |
| Arquitectura y diseño | [SAD](docs/wiki/02-arquitectura/02-SAD-HLD.md), [reglas arquitectónicas](docs/wiki/02-arquitectura/reglas-arquitectonicas.md) y [SDD](docs/wiki/04-implementacion/03-SDD-LDD.md) |
| REST, CLI o persistencia | [Contrato correspondiente](docs/wiki/03-contratos/README.md) |
| Versiones y ejecución | [Versiones](docs/wiki/04-implementacion/versiones.md) y [ejecución local](docs/wiki/04-implementacion/ejecucion-local.md) |
| Pruebas y entrega | [Pruebas](docs/wiki/04-implementacion/pruebas.md), [DoR/DoD](docs/wiki/05-proceso/dor-y-dod.md) y [lista de entrega](docs/wiki/05-proceso/entrega.md) |

## Trabajo con asistentes

- Confirma el alcance y los permisos antes de intervenir en trabajo ajeno o modificar GitHub. Aplica las [reglas de ramas y commits](docs/wiki/05-proceso/git-ramas-y-commits.md) y la [política de integración](CONTRIBUTING.md#pull-requests-y-releases).
- Puedes revisar un PR y comunicar hallazgos sin emitir un voto. Si te piden aprobar o fusionar en nombre de un colaborador, aplica las condiciones de la [revisión delegada](CONTRIBUTING.md#revisión-delegada-a-un-asistente) y detente si la identidad, la autorización o el estado del PR no coinciden.
- Trabaja dentro del alcance asignado. Comunica los defectos ajenos al cambio y pide revisión antes de alterar un contrato o una decisión.
- Informa en el chat de los cambios, pruebas, resultados y pendientes. Consulta antes de crear documentos nuevos y actualiza la fuente que mantiene cada dato. [Trabajo con IA](docs/wiki/05-proceso/trabajo-con-ia.md).
