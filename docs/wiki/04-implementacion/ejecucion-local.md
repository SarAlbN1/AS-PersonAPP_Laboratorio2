# Ejecución local: plan de reproducción

> **Estado:** HU-003 preparó y verificó el ambiente JDK 11, Maven y ambos motores. También se ejecutaron los scripts y el build (ver [ambiente local verificado](#ambiente-local-verificado-hu-003)). No se han arrancado REST ni CLI. La entrega se verifica en JDK 11. HU-004 verificó la carga inicial en ambos motores y alineó el DML de MongoDB con la base `persona_db`. El DDL de MariaDB elimina el esquema y el usuario antes de recrearlos.

## Fase 1: línea base en JDK 11

1. Clonar el [repositorio del equipo](https://github.com/SarAlbN1/AS-PersonAPP_Laboratorio2). El repositorio del docente solo fue la plantilla inicial.
2. Instalar JDK 11, Maven y los servidores MariaDB y MongoDB requeridos.
3. Registrar `java -version`, `mvn -version` y versión de ambos motores.
4. Inspeccionar y ejecutar DDL/DML, verificando el nombre de la base MongoDB.
5. Ejecutar `mvn clean install` desde la raíz multimódulo.
6. Arrancar y probar por separado `PersonAppRestApi` y `PersonAppCli`.
7. Revisar `http://localhost:3000/swagger-ui.html` en la configuración de referencia.
8. Reproducir, diagnosticar y corregir el error de Mapper indicado por el profesor.

## Ambiente local verificado (HU-003)

Procedimiento ejecutado el 2026-11-08 desde un clon limpio en Windows 11 (commit `9d44066`). Las versiones obtenidas están en [versiones](versiones.md#ambiente-de-línea-base-verificado-hu-003).

**Requisitos:** Git, Docker Desktop, JDK 11 (Temurin) y Maven 3.9.x. Maven se instala desde el zip oficial de Apache y se verifica con su SHA-512.

1. Clonar el repositorio del equipo y entrar en la carpeta.
2. Activar JDK 11 y Maven en la terminal. Si el JDK por defecto del equipo es otro, basta con fijarlos para esa sesión, sin cambiar la configuración global:

   ```powershell
   $env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-11.<versión>-hotspot'
   $env:Path = "$env:JAVA_HOME\bin;<ruta-de-maven>\bin;$env:Path"
   java -version; mvn -version
   ```

3. Levantar los motores en los puertos que esperan los `application.properties`. La contraseña de root es local: elegir un valor propio, guardarlo en una variable de la terminal y no subirlo al repositorio. En PowerShell:

   ```powershell
   $env:PERSONAPP_ROOT_PW = 'cambia-esta-clave'
   docker run -d --name personapp-mariadb -p 3307:3306 -e "MARIADB_ROOT_PASSWORD=$env:PERSONAPP_ROOT_PW" -v personapp-mariadb-data:/var/lib/mysql mariadb:11.4
   docker run -d --name personapp-mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e "MONGO_INITDB_ROOT_PASSWORD=$env:PERSONAPP_ROOT_PW" -v personapp-mongo-data:/data/db mongo:6.0
   ```

4. Ejecutar los scripts desde `scripts/` en Git Bash. La variable de PowerShell no pasa a otra terminal, así que hay que declararla de nuevo con el mismo valor. El DDL de MariaDB borra y recrea el esquema `persona_db` y su usuario.

   ```bash
   export PERSONAPP_ROOT_PW='cambia-esta-clave'
   docker exec -i personapp-mariadb mariadb -uroot -p"$PERSONAPP_ROOT_PW" < persona_ddl_maria.sql
   docker exec -i personapp-mariadb mariadb -uroot -p"$PERSONAPP_ROOT_PW" < persona_dml_maria.sql
   docker exec -i personapp-mongo mongosh --quiet -u root -p "$PERSONAPP_ROOT_PW" --authenticationDatabase admin < persona_ddl_mongo.js
   docker exec -i personapp-mongo mongosh --quiet -u root -p "$PERSONAPP_ROOT_PW" --authenticationDatabase admin < persona_dml_mongo.js
   ```

5. Comprobar el acceso con el usuario de la aplicación (`persona_db`, definido en los scripts y en los `application.properties`):

   ```bash
   docker exec personapp-mariadb mariadb -upersona_db -ppersona_db -e "SHOW TABLES FROM persona_db; SELECT COUNT(*) FROM persona_db.persona;"
   docker exec personapp-mongo mongosh --quiet -u persona_db -p persona_db --authenticationDatabase admin persona_db --eval "db.persona.countDocuments()"
   ```

6. Compilar desde la raíz: `mvn -B clean install -DskipTests`.

**Resultados observados:**
- MariaDB: las cuatro tablas creadas y cinco personas; el usuario `persona_db` accede.
- MongoDB: en la línea base de HU-003, el usuario `persona_db` se creó en `admin`. El DML original cargaba cinco personas en `prueba_db`. Para comprobar el acceso a `persona_db`, se ajustó el nombre de la base en una copia temporal del script. La HU-004 corrigió el script del repositorio.
- Build: `BUILD SUCCESS` en los ocho artefactos.

Para reiniciar los motores después de apagar el equipo: `docker start personapp-mariadb personapp-mongo`.

## Verificación de datos iniciales (HU-004)

Sigue los pasos de carga y comprobación de la sección anterior en instancias de prueba limpias. El DML actual de MongoDB carga en `persona_db.persona`, la misma base que usan REST y CLI.

Para comprobar también los registros, ejecuta `SELECT * FROM persona_db.persona ORDER BY cc` en MariaDB y `db.getSiblingDB("persona_db").persona.find().sort({_id: 1}).toArray()` en MongoDB.

El 9 de noviembre de 2026 se ejecutaron los cuatro scripts de la rama HU-004 en contenedores limpios con las imágenes `mariadb:11.4` y `mongo:6.0`. Los scripts de MongoDB se ejecutaron con `mongosh --file`. Las consultas de tablas, colección y registros se hicieron con la cuenta de aplicación `persona_db`.

| Motor | Versión observada | Estructuras y registros |
|---|---|---|
| MariaDB | 11.4.13 | `SHOW TABLES FROM persona_db` devolvió `estudios`, `persona`, `profesion` y `telefono`. `SELECT COUNT(*) FROM persona_db.persona` devolvió 5. |
| MongoDB | 6.0.28 | `getCollectionNames()` devolvió `["persona"]` en `persona_db` y `[]` en `prueba_db`. `db.persona.countDocuments()` devolvió 5. |

Los identificadores observados en ambos motores fueron 123456789, 147258369, 321654987, 963852741 y 987654321. La persona 123456789 es Pepe Perez, de 30 años. Para 987654321, MariaDB devolvió `NULL` en `edad` y MongoDB devolvió el documento sin ese campo.

## Fase 2: funcionalidad faltante

Completar CRUD y exposición REST/CLI para Persona, Profesión, Estudios y Teléfono en MariaDB y MongoDB.
