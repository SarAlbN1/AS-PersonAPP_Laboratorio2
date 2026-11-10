# Ejecución local: plan de reproducción

## Fase 1: línea base en JDK 11

1. Crear fork del repositorio del profesor: `https://github.com/andres-karoll/personapp-hexa-spring-boot`.
2. Instalar JDK 11, Maven y los servidores MariaDB y MongoDB requeridos.
3. Registrar `java -version`, `mvn -version` y versión de ambos motores.
4. Inspeccionar y ejecutar DDL/DML, verificando el nombre de la base MongoDB.
5. Ejecutar `mvn clean install` desde la raíz multimódulo.
6. Arrancar y probar por separado `PersonAppRestApi` y `PersonAppCli`.
7. Revisar `http://localhost:3000/swagger-ui.html` en la configuración de referencia.
8. Reproducir, diagnosticar y corregir el error de Mapper indicado por el profesor.

## Verificación de datos iniciales (HU-004)

Usa instancias locales de prueba. El DDL de MariaDB recrea la base y la cuenta del laboratorio. El DDL de MongoDB crea la cuenta en `admin` con permisos sobre `persona_db`. Ejecuta la carga en una instancia limpia para evitar colisiones con la cuenta o los identificadores de ejemplo.

Desde la raíz del repositorio, ejecuta los scripts con las cuentas administrativas de las instancias de prueba. Ajusta el usuario y los puertos según la HU-003.

```sh
mariadb -h 127.0.0.1 -P 3307 -u root -p < scripts/persona_ddl_maria.sql
mariadb -h 127.0.0.1 -P 3307 -u root -p < scripts/persona_dml_maria.sql
mongosh --host 127.0.0.1 --port 27017 --file scripts/persona_ddl_mongo.js
mongosh --host 127.0.0.1 --port 27017 --file scripts/persona_dml_mongo.js
```

Si MongoDB requiere autenticación, añade la cuenta administrativa y su base de autenticación a ambos comandos. El DML carga en `persona_db.persona`, la misma base configurada en REST y CLI. Antes seleccionaba `prueba_db`.

En MariaDB, verifica las tablas `persona`, `profesion`, `telefono` y `estudios` con `SHOW TABLES FROM persona_db`, y los cinco registros con `SELECT * FROM persona_db.persona ORDER BY cc`. En MongoDB, consulta `db.getSiblingDB("persona_db").getCollectionNames()` y `db.getSiblingDB("persona_db").persona.find().sort({_id: 1}).toArray()`. ### Resultado observado

El 9 de noviembre de 2026 se ejecutaron los cuatro scripts de la rama HU-004 en contenedores limpios con las imágenes `mariadb:11.4` y `mongo:6.0`. Los scripts de MongoDB se ejecutaron con `mongosh --file`. Las consultas de tablas, colección y registros se hicieron con la cuenta de aplicación `persona_db`.

| Motor | Versión observada | Estructuras y registros |
|---|---|---|
| MariaDB | 11.4.13 | `SHOW TABLES FROM persona_db` devolvió `estudios`, `persona`, `profesion` y `telefono`. `SELECT COUNT(*) FROM persona_db.persona` devolvió 5. |
| MongoDB | 6.0.28 | `getCollectionNames()` devolvió `["persona"]` en `persona_db` y `[]` en `prueba_db`. `db.persona.countDocuments()` devolvió 5. |

Los identificadores observados en ambos motores fueron 123456789, 147258369, 321654987, 963852741 y 987654321. La persona 123456789 es Pepe Perez, de 30 años. Para 987654321, MariaDB devolvió `NULL` en `edad` y MongoDB devolvió el documento sin ese campo.

## Fase 2: funcionalidad faltante

Completar CRUD y exposición REST/CLI para Persona, Profesión, Estudios y Teléfono en MariaDB y MongoDB.

**Pendiente:** no se dispone de registros de ejecución del equipo; añadir comandos realmente usados, fallos, salidas y capturas.
