# Reglas arquitectónicas y comprobación

**Proyecto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de diseño basada en enunciado y plantilla de referencia; verificación de implementación pendiente  
[← Arquitectura](README.md) · [SRS](../01-producto/01-SRS.md) · [Backlog](../06-backlog/README.md)


## Reglas obligatorias de la solución

| ID | Regla | Método de comprobación | HU |
|---|---|---|---|
| AR-01 | `domain` no importa HTTP, CLI, Spring/JPA ni clases de Mongo | Inspección de imports/dependencias Maven | HU-027 |
| AR-02 | `application` depende de modelos/puertos, no de repositorios tecnológicos concretos | Revisar POM, tipos inyectados, imports | HU-027 |
| AR-03 | Cada acceso a MariaDB/MongoDB atraviesa un adaptador de salida que implementa el puerto requerido | Revisión de interfaces, integración real | HU-013/017/020/023 |
| AR-04 | REST y CLI no contienen reglas de negocio duplicadas; delegan en los casos de uso | Pruebas de consistencia y revisión | HU-014/015/018/021/024 |
| AR-05 | DTO y entidades persistentes no se devuelven al núcleo como dependencias de framework | Revisión de mapper/contratos | HU-006/027 |
| AR-06 | Motor elegido por operación/sesión de CLI sin cambiar configuración global que afecte otras peticiones | Alternancia y concurrencia cuando aplique | HU-025 |
| AR-07 | Ambas persistencias deben respetar identidad y relaciones acordadas del dominio | Casos positivos y negativos de datos | HU-011/026 |
| AR-08 | Los endpoints implementados coinciden con Swagger/OpenAPI y sus códigos de estado | Pruebas HTTP y revisión OpenAPI | HU-028 |
| AR-09 | El proyecto original debe compilarse y ejecutarse antes de actualizar versiones | Registro de baseline | HU-005/007 |
| AR-10 | No se añaden microservicios, broker de eventos, bases extra ni componentes fuera del laboratorio | POM y revisión de PR | HU-027 |
| AR-11 | No se incluyen contraseñas, tokens ni datos sensibles en Git o logs de prueba | Revisión de cambios | HU-029/032 |
| AR-12 | No se modifica un contrato arquitectónico sin revisar SRS/SAD/ADR/contratos y las pruebas afectadas | Checklist del PR | Transversal |

## Distinciones técnicas

- **`Service`** representa un caso de uso de aplicación; **`Repository`** encapsula acceso tecnológico. No sustituir uno por otro.
- **Un selector de persistencia no equivale a CQRS.** CQRS separa modelos/operaciones de lectura y escritura; aquí se escoge un adaptador según motor.
- **Módulo Maven ≠ microservicio**. REST y CLI pueden tener arranques distintos sin que exista arquitectura distribuida entre ellos.
- **Dos bases ≠ replicación**. Una persona guardada en MariaDB no aparece automáticamente en MongoDB.

## Checklist de PR

- [ ] La HU/issue y los criterios de aceptación están identificados.
- [ ] No se invierte ninguna dependencia `domain`/`application` hacia infraestructura.
- [ ] Adaptadores REST/CLI y Maria/Mongo cumplen contratos compartidos.
- [ ] Se prueban éxito y errores con el motor correspondiente.
- [ ] Cambio de motor no exige reiniciar ni afecta solicitudes concurrentes indebidamente.
- [ ] Cualquier cambio de endpoint, esquema o versión actualiza contrato, ADR y pruebas.
- [ ] No se incluyen secretos ni se afirma ejecución sin evidencia.

**Política:** una infracción bloquea la integración hasta corregirse o aprobar una revisión formal de arquitectura. La política de PR se administra en [05-proceso](../05-proceso/README.md).
