# Plan de pruebas del Laboratorio 2

## Matriz objetivo (no son pruebas ejecutadas)

| Entidad | Operaciones | Clientes | Motores |
|---|---|---|---|
| Persona | C/R/U/D | REST y CLI | MariaDB y MongoDB |
| Profesión | C/R/U/D | REST y CLI | MariaDB y MongoDB |
| Estudios | C/R/U/D | REST y CLI | MariaDB y MongoDB |
| Teléfono | C/R/U/D | REST y CLI | MariaDB y MongoDB |

## Casos transversales

- Identificadores inexistentes y entradas inválidas.
- Integridad de relaciones entre persona, profesión, estudios y teléfono.
- Round-trip de conversiones DTO ↔ dominio ↔ entidad/documento.
- Alternancia MariaDB → MongoDB → MariaDB sin reiniciar.
- Regresión del Mapper inicialmente defectuoso.
- Validación de Swagger 3 con los endpoints efectivos en JDK 11.

## Evidencias pendientes

Para cada caso: ID, prerequisito, datos de entrada, pasos, resultado esperado, resultado real, motor, cliente, fecha, commit y captura/log.
