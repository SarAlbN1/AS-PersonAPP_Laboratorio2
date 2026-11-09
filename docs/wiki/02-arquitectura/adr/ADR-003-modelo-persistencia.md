# ADR-003 · Representación de entidades en MariaDB y MongoDB

**Estado:** Pendiente de definición · HU-011, HU-017, HU-020, HU-023, HU-026

## Problema

MariaDB utiliza tablas y FK, incluido Estudios con identidad compuesta `(id_prof, cc_per)`; MongoDB usa documentos. Hay que conservar reglas de negocio sin acoplar el dominio a una representación física.

## Alternativas por evaluar

- Referencias entre documentos/colecciones con validaciones en caso de uso.
- Embebido de relaciones documentales cuando encaje con operaciones y cardinalidades.
- Identificadores físicos de Mongo separados del identificador estable del dominio, mediante Mapper.

## Criterios que debe cumplir cualquier decisión

No perder identidad, teléfono dueño ni asociación Estudios–Profesión–Persona; respetar unicidad y borrado acordados; garantizar que CRUD en ambos motores produce semántica de dominio equivalente. No inventar sincronización entre bases.

## Pendiente antes de aprobar

Leer scripts y repositorios reales, decidir estructura de colecciones/índices, documentar comportamiento de integridad y preparar pruebas de relación y errores. Actualizar [persistencia](../../03-contratos/persistencia.md) y el SDD en el mismo PR.
