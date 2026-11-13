# SDD / LDD · Diseño existente y brechas

Inspección estática del 2026-11-08. [Inventario completo de clases](estructura-del-repositorio.md). Este documento describe código existente; las correcciones propuestas todavía no se implementan.

## Núcleo y composición

`Person`, `Profession`, `Phone`, `Study` y `Gender` ya existen con Lombok. `Person.isValidAge()` compara `age >= 0` sin manejar null, aunque edad es opcional en SQL. Lombok `@NonNull` no sustituye validaciones de formato, rangos o referencias; existe constructor sin argumentos.

`PersonInputPort` declara `create`, `edit`, `drop`, `findAll`, `findOne`, `count`, `getPhones`, `getStudies` y `setPersintence` (ortografía original). `PersonUseCase` implementa esas operaciones. `PersonOutputPort` declara `save`, `delete`, `find`, `findById`; ambos adaptadores de Persona lo implementan. No existen puertos, casos de uso ni repositories independientes para Teléfono o Estudios.

**Profesión (HU-016).** `ProfessionInputPort` declara `create`, `edit`, `drop`, `findAll`, `findPage`, `findOne` y `count`. `ProfessionOutputPort` declara `save`, `delete`, `find()` ordenado por id, `find(page, size)`, `findById`, `count` y `hasStudies`; lo implementan `ProfessionOutputAdapterMaria` y `ProfessionOutputAdapterMongo` (HU-017). `ProfessionUseCase` aplica las reglas C-02 a C-08 de HU-011 con las excepciones de `common` `InvalidDataException` (ERR-01), `DuplicateException` (ERR-04), `NoExistException` (ERR-03), `DependentRecordsException` (ERR-06) y `KeyMismatchException` (ERR-09). No lleva `@UseCase` ni `@Qualifier`: el adaptador de entrada lo crea en cada operación con el puerto del motor elegido, sin guardar el caso de uso en un campo compartido. `ProfessionUseCaseTest` cubre las seis operaciones sobre un puerto en memoria.

El constructor de `PersonUseCase` recibe el puerto con `@Qualifier("personOutputAdapterMaria")`. Las dos entradas crean manualmente `new PersonUseCase(puertoSeleccionado)`. No hay una fábrica ni selector independiente. Se reutiliza esta capacidad como referencia, sin certificar aislamiento concurrente: REST guarda `personInputPort` en un campo mutable de su adaptador Spring compartido.

## REST existente

`PersonAppRestApi` arranca una aplicación Spring. `PersonaControllerV1` declara solo `GET /api/v1/persona/{database}` y `POST /api/v1/persona`. `PersonaInputAdapterRest` selecciona salida por path en GET y por `request.database` en POST. GET transforma la opción a mayúsculas; el selector compara sin distinguir mayúsculas.

`PersonaRequest` tiene seis campos String: `dni`, `firstName`, `lastName`, `age`, `sex`, `database`; `PersonaResponse` hereda esos campos y añade `status`. `Response` es otro modelo que el controlador actual no devuelve. `PersonaMapperRest.fromAdapterToDomain` devuelve `new Person()` sin copiar datos. POST usa siempre el mapper de respuesta MariaDB, incluso al seleccionar MongoDB.

## CLI existente

`PersonAppCli` implementa `CommandLineRunner` y llama `MenuPrincipal`. Opciones principales: 0 salir, 1 Persona, 2 Profesión, 3 Teléfono, 4 Estudios. Las tres últimas solo registran `Implementar Menu`. En Persona: 1 MariaDB, 2 MongoDB, 0 regresar; después 1 listar, 0 regresar. `PersonaInputAdapterCli.historial` usa `findAll` y `PersonaMapperCli`; `historial1` es una variante existente, no otra funcionalidad.

`leerOpcion` captura `InputMismatchException` y recurre sin consumir el token incorrecto: riesgo de repetición y agotamiento de pila, pendiente de prueba.

## Persistencia y mappers

MariaDB contiene cinco clases JPA (incluida `EstudiosEntityPK`), cuatro mappers y los repositories de Persona y Profesión. `PersonaEntity` tiene asociaciones con cascada `ALL`; no debe suponerse que el borrado coincide con las FK restrictivas del DDL sin probar el camino JPA. `ProfesionEntity` ya no cascadea `REMOVE` (D-02, HU-017): con `ALL`, borrar una profesión con estudios eliminaba también sus estudios, y ahora la FK lo rechaza. MongoDB contiene cuatro documentos, cuatro mappers y los repositories de Persona y Profesión; usa `@DocumentReference` y colecciones inversas `@ReadOnlyProperty`.

Los mappers de los dos motores cortan las referencias bidireccionales (#47). Una persona o una profesión leída por sí misma trae sus colecciones. Cuando aparece como dueño de un teléfono, o como persona o profesión de un estudio, se usa `fromAdapterToDomainBasic`, que copia sus atributos y deja las colecciones en `null` porque no se cargan. Al escribir, el dueño de un teléfono y la persona y la profesión de un estudio se convierten con `fromDomainToAdapterReference`, que solo lleva la clave: es lo que usan la FK de MariaDB y el `@DocumentReference` de MongoDB. Los dos `EstudiosMapper*.fromAdapterToDomain` devuelven el `Study` construido, y el de MariaDB convierte la fecha que JPA entrega como `java.sql.Date`. **Adaptadores de Profesión (HU-017).** Implementan `ProfessionOutputPort` en los dos motores y devuelven las profesiones con `fromAdapterToDomainBasic`, sin estudios (C-10). Listar todos y la página piden el orden por id al motor con `Sort` y `PageRequest` (C-06, C-07); el conteo usa `count()` del repositorio (C-08). `hasStudies` consulta los dependientes sin cargar la colección: en MariaDB con una consulta JPQL sobre `EstudiosEntity`, y en MongoDB con `MongoTemplate.exists` sobre `primaryProfesion`, donde `@DocumentReference` guarda el id. El adaptador de MongoDB no captura los errores de escritura. `ProfessionOutputAdapter{Maria,Mongo}Test` prueban la lógica con Mockito.

Las propiedades siguen permitiendo referencias circulares de beans, pero ya no hay recursión de datos. Spring Data MongoDB convierte `LocalDate` en la zona horaria de la JVM, así que una misma fecha se guarda distinto en una máquina local y en un contenedor en UTC. Queda para HU-023.

`PersonOutputAdapterMongo.save` captura `MongoWriteException` y devuelve la entrada, de modo que ese camino puede aparentar éxito sin persistencia. `PersonUseCase.create` no distingue alta duplicada de `save`; `edit` consulta la clave del argumento pero guarda el objeto recibido sin garantizar igualdad de identidades.

## Diseño pendiente

Reproducir defectos y probar regresiones antes de corregirlos. Las validaciones, errores y reglas comunes se aprobaron en HU-011 ([persistencia §6.1](../03-contratos/persistencia.md#61-reglas-comunes--aprobado-hu-011), [validaciones §2.1](../03-contratos/validaciones-y-errores.md#21-tipos-y-límites--aprobado-hu-011) y [ADR-003](../02-arquitectura/adr/ADR-003-modelo-persistencia.md)); las HU de cada recurso las implementan. Revisar aislamiento del selector; completar exposición de Persona reutilizando sus puertos y adaptadores; ampliar los otros tres recursos desde sus modelos y representaciones existentes. No aprobar versiones o rutas nuevas desde este SDD.
