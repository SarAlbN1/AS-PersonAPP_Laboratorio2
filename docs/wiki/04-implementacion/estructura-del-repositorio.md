# Estructura e inventario real del repositorio

Inspección estática: 2026-11-08. Referencia Git: `b9bc9c824b8005f504163ca4cb67801c7b026df8`; la documentación añadida estaba sin seguimiento. No acredita compilación ni ejecución.

## Estructura conservada

```text
pom.xml                         Agregador Maven original
README.md                       Instrucciones originales + orientación
CLAUDE.md / CONTRIBUTING.md
common/
domain/
application/
maria-output-adapter/
mongo-output-adapter/
rest-input-adapter/
cli-input-adapter/
scripts/                        Cuatro DDL/DML originales
docs/
  informe/                      Informe del equipo; no sustituye al enunciado
  sustentacion/
  wiki/
    Home.md
    01-producto/
    02-arquitectura/
    03-contratos/
    04-implementacion/
    05-proceso/
    06-backlog/
```

## Todas las clases e interfaces Java existentes

### `common` · 7 archivos Java

- [Adapter](../../../common/src/main/java/co/edu/javeriana/as/personapp/common/annotations/Adapter.java): `co/edu/javeriana/as/personapp/common/annotations/Adapter.java`.
- [Mapper](../../../common/src/main/java/co/edu/javeriana/as/personapp/common/annotations/Mapper.java): `co/edu/javeriana/as/personapp/common/annotations/Mapper.java`.
- [Port](../../../common/src/main/java/co/edu/javeriana/as/personapp/common/annotations/Port.java): `co/edu/javeriana/as/personapp/common/annotations/Port.java`.
- [UseCase](../../../common/src/main/java/co/edu/javeriana/as/personapp/common/annotations/UseCase.java): `co/edu/javeriana/as/personapp/common/annotations/UseCase.java`.
- [InvalidOptionException](../../../common/src/main/java/co/edu/javeriana/as/personapp/common/exceptions/InvalidOptionException.java): `co/edu/javeriana/as/personapp/common/exceptions/InvalidOptionException.java`.
- [NoExistException](../../../common/src/main/java/co/edu/javeriana/as/personapp/common/exceptions/NoExistException.java): `co/edu/javeriana/as/personapp/common/exceptions/NoExistException.java`.
- [DatabaseOption](../../../common/src/main/java/co/edu/javeriana/as/personapp/common/setup/DatabaseOption.java): `co/edu/javeriana/as/personapp/common/setup/DatabaseOption.java`.

### `domain` · 5 archivos Java

- [Gender](../../../domain/src/main/java/co/edu/javeriana/as/personapp/domain/Gender.java): `co/edu/javeriana/as/personapp/domain/Gender.java`.
- [Person](../../../domain/src/main/java/co/edu/javeriana/as/personapp/domain/Person.java): `co/edu/javeriana/as/personapp/domain/Person.java`.
- [Phone](../../../domain/src/main/java/co/edu/javeriana/as/personapp/domain/Phone.java): `co/edu/javeriana/as/personapp/domain/Phone.java`.
- [Profession](../../../domain/src/main/java/co/edu/javeriana/as/personapp/domain/Profession.java): `co/edu/javeriana/as/personapp/domain/Profession.java`.
- [Study](../../../domain/src/main/java/co/edu/javeriana/as/personapp/domain/Study.java): `co/edu/javeriana/as/personapp/domain/Study.java`.

### `application` · 3 archivos Java

- [PersonInputPort](../../../application/src/main/java/co/edu/javeriana/as/personapp/application/port/in/PersonInputPort.java): `co/edu/javeriana/as/personapp/application/port/in/PersonInputPort.java`.
- [PersonOutputPort](../../../application/src/main/java/co/edu/javeriana/as/personapp/application/port/out/PersonOutputPort.java): `co/edu/javeriana/as/personapp/application/port/out/PersonOutputPort.java`.
- [PersonUseCase](../../../application/src/main/java/co/edu/javeriana/as/personapp/application/usecase/PersonUseCase.java): `co/edu/javeriana/as/personapp/application/usecase/PersonUseCase.java`.

### `maria-output-adapter` · 11 archivos Java

- [PersonOutputAdapterMaria](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/adapter/PersonOutputAdapterMaria.java): `co/edu/javeriana/as/personapp/mariadb/adapter/PersonOutputAdapterMaria.java`.
- [EstudiosEntity](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/entity/EstudiosEntity.java): `co/edu/javeriana/as/personapp/mariadb/entity/EstudiosEntity.java`.
- [EstudiosEntityPK](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/entity/EstudiosEntityPK.java): `co/edu/javeriana/as/personapp/mariadb/entity/EstudiosEntityPK.java`.
- [PersonaEntity](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/entity/PersonaEntity.java): `co/edu/javeriana/as/personapp/mariadb/entity/PersonaEntity.java`.
- [ProfesionEntity](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/entity/ProfesionEntity.java): `co/edu/javeriana/as/personapp/mariadb/entity/ProfesionEntity.java`.
- [TelefonoEntity](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/entity/TelefonoEntity.java): `co/edu/javeriana/as/personapp/mariadb/entity/TelefonoEntity.java`.
- [EstudiosMapperMaria](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/mapper/EstudiosMapperMaria.java): `co/edu/javeriana/as/personapp/mariadb/mapper/EstudiosMapperMaria.java`.
- [PersonaMapperMaria](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/mapper/PersonaMapperMaria.java): `co/edu/javeriana/as/personapp/mariadb/mapper/PersonaMapperMaria.java`.
- [ProfesionMapperMaria](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/mapper/ProfesionMapperMaria.java): `co/edu/javeriana/as/personapp/mariadb/mapper/ProfesionMapperMaria.java`.
- [TelefonoMapperMaria](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/mapper/TelefonoMapperMaria.java): `co/edu/javeriana/as/personapp/mariadb/mapper/TelefonoMapperMaria.java`.
- [PersonaRepositoryMaria](../../../maria-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mariadb/repository/PersonaRepositoryMaria.java): `co/edu/javeriana/as/personapp/mariadb/repository/PersonaRepositoryMaria.java`.

### `mongo-output-adapter` · 10 archivos Java

- [PersonOutputAdapterMongo](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/adapter/PersonOutputAdapterMongo.java): `co/edu/javeriana/as/personapp/mongo/adapter/PersonOutputAdapterMongo.java`.
- [EstudiosDocument](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/document/EstudiosDocument.java): `co/edu/javeriana/as/personapp/mongo/document/EstudiosDocument.java`.
- [PersonaDocument](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/document/PersonaDocument.java): `co/edu/javeriana/as/personapp/mongo/document/PersonaDocument.java`.
- [ProfesionDocument](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/document/ProfesionDocument.java): `co/edu/javeriana/as/personapp/mongo/document/ProfesionDocument.java`.
- [TelefonoDocument](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/document/TelefonoDocument.java): `co/edu/javeriana/as/personapp/mongo/document/TelefonoDocument.java`.
- [EstudiosMapperMongo](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/mapper/EstudiosMapperMongo.java): `co/edu/javeriana/as/personapp/mongo/mapper/EstudiosMapperMongo.java`.
- [PersonaMapperMongo](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/mapper/PersonaMapperMongo.java): `co/edu/javeriana/as/personapp/mongo/mapper/PersonaMapperMongo.java`.
- [ProfesionMapperMongo](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/mapper/ProfesionMapperMongo.java): `co/edu/javeriana/as/personapp/mongo/mapper/ProfesionMapperMongo.java`.
- [TelefonoMapperMongo](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/mapper/TelefonoMapperMongo.java): `co/edu/javeriana/as/personapp/mongo/mapper/TelefonoMapperMongo.java`.
- [PersonaRepositoryMongo](../../../mongo-output-adapter/src/main/java/co/edu/javeriana/as/personapp/mongo/repository/PersonaRepositoryMongo.java): `co/edu/javeriana/as/personapp/mongo/repository/PersonaRepositoryMongo.java`.

### `rest-input-adapter` · 7 archivos Java

- [PersonAppRestApi](../../../rest-input-adapter/src/main/java/co/edu/javeriana/as/personapp/PersonAppRestApi.java): `co/edu/javeriana/as/personapp/PersonAppRestApi.java`.
- [PersonaInputAdapterRest](../../../rest-input-adapter/src/main/java/co/edu/javeriana/as/personapp/adapter/PersonaInputAdapterRest.java): `co/edu/javeriana/as/personapp/adapter/PersonaInputAdapterRest.java`.
- [PersonaControllerV1](../../../rest-input-adapter/src/main/java/co/edu/javeriana/as/personapp/controller/PersonaControllerV1.java): `co/edu/javeriana/as/personapp/controller/PersonaControllerV1.java`.
- [PersonaMapperRest](../../../rest-input-adapter/src/main/java/co/edu/javeriana/as/personapp/mapper/PersonaMapperRest.java): `co/edu/javeriana/as/personapp/mapper/PersonaMapperRest.java`.
- [PersonaRequest](../../../rest-input-adapter/src/main/java/co/edu/javeriana/as/personapp/model/request/PersonaRequest.java): `co/edu/javeriana/as/personapp/model/request/PersonaRequest.java`.
- [PersonaResponse](../../../rest-input-adapter/src/main/java/co/edu/javeriana/as/personapp/model/response/PersonaResponse.java): `co/edu/javeriana/as/personapp/model/response/PersonaResponse.java`.
- [Response](../../../rest-input-adapter/src/main/java/co/edu/javeriana/as/personapp/model/response/Response.java): `co/edu/javeriana/as/personapp/model/response/Response.java`.

### `cli-input-adapter` · 6 archivos Java

- [PersonAppCli](../../../cli-input-adapter/src/main/java/co/edu/javeriana/as/personapp/PersonAppCli.java): `co/edu/javeriana/as/personapp/PersonAppCli.java`.
- [PersonaInputAdapterCli](../../../cli-input-adapter/src/main/java/co/edu/javeriana/as/personapp/terminal/adapter/PersonaInputAdapterCli.java): `co/edu/javeriana/as/personapp/terminal/adapter/PersonaInputAdapterCli.java`.
- [PersonaMapperCli](../../../cli-input-adapter/src/main/java/co/edu/javeriana/as/personapp/terminal/mapper/PersonaMapperCli.java): `co/edu/javeriana/as/personapp/terminal/mapper/PersonaMapperCli.java`.
- [MenuPrincipal](../../../cli-input-adapter/src/main/java/co/edu/javeriana/as/personapp/terminal/menu/MenuPrincipal.java): `co/edu/javeriana/as/personapp/terminal/menu/MenuPrincipal.java`.
- [PersonaMenu](../../../cli-input-adapter/src/main/java/co/edu/javeriana/as/personapp/terminal/menu/PersonaMenu.java): `co/edu/javeriana/as/personapp/terminal/menu/PersonaMenu.java`.
- [PersonaModelCli](../../../cli-input-adapter/src/main/java/co/edu/javeriana/as/personapp/terminal/model/PersonaModelCli.java): `co/edu/javeriana/as/personapp/terminal/model/PersonaModelCli.java`.

## Dependencias internas declaradas

`application → domain + common`; `maria-output-adapter → application`; `mongo-output-adapter → application`; cada entrada depende de `application` y de los dos adaptadores de salida. `domain` no declara dependencias propias, pero hereda Lombok y Log4j del padre. `common` declara Spring Context; `application` usa `@Qualifier` y `@UseCase` basado en Spring. No equivale a un núcleo totalmente independiente de frameworks.

No existen archivos `src/test` ni Maven Wrapper en esta copia. Hay dos clases de arranque Spring, no siete servicios. Los cuatro scripts son `persona_ddl_maria.sql`, `persona_dml_maria.sql`, `persona_ddl_mongo.js` y `persona_dml_mongo.js`.
