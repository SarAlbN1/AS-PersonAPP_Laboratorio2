# Reglas arquitectónicas y comprobación

**Proyecto:** PersonAPP · Laboratorio 2  
**Estado:** especificación de diseño basada en enunciado y plantilla de referencia; dependencias hexagonales verificadas en HU-027 (ver [revisión](#revisión-de-hu-027--dependencias-hexagonales-y-mappers))  
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

## Preservación obligatoria de la arquitectura

Todas las HU conservan exactamente los módulos `common`, `domain`, `application`, `rest-input-adapter`, `cli-input-adapter`, `maria-output-adapter` y `mongo-output-adapter`, sus responsabilidades y las dependencias hexagonales. No se permite añadir, eliminar, fusionar o renombrar módulos ni trasladar responsabilidades entre capas. Las migraciones y los contenedores deben adaptarse a esta estructura. Un ADR no constituye una excepción automática: cualquier propuesta que cambie esta restricción requiere una nueva instrucción expresa del responsable del proyecto antes de implementarse.

HU-027 comprueba esta regla y su incumplimiento bloquea el cierre de cualquier HU afectada y de la entrega.

## Revisión de HU-027 · dependencias hexagonales y mappers

Se revisó el 16 de noviembre de 2026 sobre `main` (`1a112e4`) con la rama de HU-024 (#67) integrada, para cubrir los cuatro recursos y la consulta de HU-033. La revisión combina el POM de cada módulo, los `import` del código de producción y las pruebas de motor con MariaDB y MongoDB reales.

| Módulo | Depende de (Maven) | Comprobación | Resultado |
|---|---|---|---|
| `common` | `spring-context` | Solo anotaciones (`@Adapter`, `@Mapper`, `@Port`, `@UseCase`) y excepciones del catálogo | Cumple. Es la dependencia de Spring que la plantilla concentra aquí para que el núcleo no la importe |
| `domain` | Lombok (del POM padre) | Ningún `import` fuera de `java.*` y Lombok | Cumple AR-01 |
| `application` | `domain`, `common` | Ningún `import` de Spring, JPA, Mongo ni de los adaptadores. Los puertos solo usan tipos del dominio, sin `Page`, `Sort`, `Optional`, entidades ni documentos | Cumple AR-02 después de la corrección de abajo |
| `maria-output-adapter` | `application`, Spring Data JPA, driver MariaDB | Implementa los cuatro puertos de salida. No importa REST, CLI ni MongoDB | Cumple AR-03 |
| `mongo-output-adapter` | `application`, Spring Data MongoDB | Implementa los cuatro puertos de salida. No importa REST, CLI ni MariaDB | Cumple AR-03 |
| `rest-input-adapter` | `application`, los dos adaptadores de salida, Spring Web, springdoc | El código solo usa puertos y casos de uso, nunca clases de `mariadb` o `mongo`. Depende de esos módulos para armar la aplicación, porque es un ejecutable | Cumple AR-04 y AR-05 |
| `cli-input-adapter` | `application`, los dos adaptadores de salida, Spring Boot | Igual que REST | Cumple AR-04 y AR-05 |

**Puertos e implementaciones.** `PersonOutputPort`, `PhoneOutputPort`, `ProfessionOutputPort` y `StudyOutputPort` tienen un adaptador en cada motor. Los cinco puertos de entrada tienen su caso de uso (`Person`, `PersonPhones`, `Phone`, `Profession` y `Study`). REST y CLI crean el caso de uso en cada operación con los puertos del motor pedido; ningún adaptador de entrada guarda el caso de uso ni el motor en un campo (AR-06, comprobado por concurrencia en HU-025).

**Mappers.** Cada módulo adaptador tiene los suyos: REST (`*MapperRest`, cinco), CLI (`*MapperCli`, cuatro), MariaDB y MongoDB (`*MapperMaria` y `*MapperMongo`, cuatro cada uno). Los de REST y CLI solo convierten la entrada; ninguno valida longitudes, rangos ni duplicados, que son del caso de uso (AR-04). Los de persistencia cortan las referencias bidireccionales con `fromAdapterToDomainBasic` (#47, `MapperRecursion{Maria,Mongo}Test`) y las entidades y documentos no salen de su módulo (AR-05).

**AR-10.** Siguen los siete módulos de la plantilla. Docker Compose (HU-034) solo agrega los contenedores de la aplicación y de las dos bases.

**Defecto corregido.** `PersonUseCase`, heredado de la plantilla, era el único punto de `application` que importaba Spring. Tenía `@UseCase`, que lo registraba como bean, y `@Qualifier("personOutputAdapterMaria")`, que lo ataba por nombre al adaptador de MariaDB. Además `PersonInputPort` exponía `setPersintence`, que permitía cambiar el motor de un caso de uso ya creado. Ningún adaptador lo usaba así, pero contradecía AR-02 y C-09. HU-027 quitó las dos anotaciones y `setPersintence`, y dejó el puerto como campo `final` fijado en el constructor, igual que los otros cuatro casos de uso. `PersonUseCaseTest.cadaCasoDeUsoQuedaAtadoASuPuerto` reemplaza la prueba del setter.

**Cómo repetirla.** Desde la raíz del repositorio, cada consulta debe devolver cero líneas:

```bash
grep -rhE '^import ' domain/src/main/java | grep -vE 'import (java\.|lombok|co\.edu)'
grep -rlE '^import (org\.springframework|jakarta\.persistence|org\.bson|com\.mongodb)' application/src/main/java
grep -rnE 'import co\.edu\.javeriana\.as\.personapp\.(mariadb|mongo)\.' rest-input-adapter/src/main cli-input-adapter/src/main
grep -rnE 'import co\.edu\.javeriana\.as\.personapp\.(controller|terminal|model)\.' maria-output-adapter/src/main mongo-output-adapter/src/main
grep -rnE 'Entity|Document|Pageable|Page<|Sort|Optional' application/src/main/java/co/edu/javeriana/as/personapp/application/port
```

Después, `./mvnw -B -ntp clean install -Dmotores=arriba` con las bases de `compose.pruebas.yaml` dio `BUILD SUCCESS` sin fallos. Además se probó por REST en Docker Compose que Persona sigue respondiendo en los dos motores, incluido `409` ERR-06 al borrar una persona con dependientes.

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

**Política:** una infracción bloquea la integración hasta corregirse. La preservación modular y hexagonal es obligatoria para este alcance. La política de PR se administra en [05-proceso](../05-proceso/README.md).
