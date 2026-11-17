# Evidencias de pruebas PersonAPP

Solo registra resultados observados. [← Pruebas](../pruebas.md) · [Registro de evidencias](../registro-de-evidencias.md)

## Verificación final de las seis operaciones (HU-026)

**Fecha:** 2026-11-17 · **Código verificado:** `main` en `92b1454` (con HU-024, HU-025 y HU-027 integradas) · **Entorno:** Docker Compose del repositorio, con REST y CLI en Java 25.0.5 (Temurin), MariaDB 11.4 y MongoDB 6.0. Las bases parten de los DDL y DML del repositorio y volúmenes nuevos.

Dos guiones en `scripts/verificacion/` ejecutan todas las comprobaciones y comparan cada resultado con el esperado. Cada fila de las tablas de abajo es su salida, sin editar; en los encabezados solo se quitó la ruta local del archivo de entorno.

| Guion | Qué recorre | Resultado |
|---|---|---|
| `hu026-rest.sh` | Las seis operaciones y el listado parcial de los cuatro recursos en `MARIA` y en `MONGO`, 23 casos negativos por motor, la consulta Persona–Teléfonos y la paginación con tres registros | **162 de 162 correctas** |
| `hu026-cli.sh` | Lo mismo desde el menú, en **una sola sesión** que pasa de MariaDB a MongoDB y vuelve sin reiniciar la aplicación | **123 de 123 correctas**, la sesión termina con código 0 |

Los dos guiones crean registros propios (cc `880000001`, profesión `880`, teléfono `0088000001` y estudio `(880, 880000001)`) y los eliminan al terminar. Después de correr la CLI se volvió a correr el guion REST y dio el mismo resultado, con los conteos iniciales intactos (5 personas, 2 profesiones, 3 teléfonos y 2 estudios en cada motor).

### Cómo repetirla

```bash
cp .env.example .env              # y cambiar la clave
docker compose up -d --build --wait
bash scripts/verificacion/hu026-rest.sh http://localhost:3000
bash scripts/verificacion/hu026-cli.sh            # usa: docker compose run -T --rm --build cli
docker compose --profile cli down -v
```

Sin contenedores para la aplicación, se pasa la URL del JAR REST y el comando del JAR de la CLI: `bash scripts/verificacion/hu026-cli.sh "java -jar cli-input-adapter/target/cli-input-adapter-0.0.1-SNAPSHOT-exec.jar"`. Los guiones necesitan `bash`, `curl` y `python` (solo para contar elementos de un arreglo JSON), y terminan con código 1 si falla alguna comprobación.

### Matriz de las 96 combinaciones

4 recursos × 6 operaciones × 2 entradas × 2 motores. Cada celda da los ID de las comprobaciones; el prefijo `MA-` es MariaDB y `MO-` es MongoDB. Todas están **OK** en los dos motores.

| Recurso | Entrada | Crear | Consultar por identificador | Listar todos | Contar | Editar | Eliminar | Adicional: listar parcial |
|---|---|---|---|---|---|---|---|---|
| Persona | REST | PE-01 | PE-02, PE-08 | PE-04 | PE-06, PE-11 | PE-07 | PE-09, PE-10 | PE-05 |
| Persona | CLI | PE-02 | PE-03, PE-10 | PE-06 | PE-01, PE-11, PE-14 | PE-08, PE-09 | PE-12, PE-13 | PE-07 |
| Profesión | REST | PR-01 | PR-02, PR-08 | PR-04 | PR-06, PR-11 | PR-07 | PR-09, PR-10 | PR-05 |
| Profesión | CLI | PR-02 | PR-03, PR-07 | PR-04 | PR-01, PR-08, PR-11 | PR-06 | PR-09, PR-10 | PR-05 |
| Teléfono | REST | TE-01 | TE-02, TE-08 | TE-04 | TE-06, TE-11 | TE-07 | TE-09, TE-10 | TE-05 |
| Teléfono | CLI | TE-02 | TE-03, TE-08 | TE-04 | TE-01, TE-09, TE-12 | TE-07 | TE-10, TE-11 | TE-05, TE-06 |
| Estudios | REST | ES-01 | ES-02, ES-08 | ES-04 | ES-06, ES-11 | ES-07 | ES-09, ES-10 | ES-05 |
| Estudios | CLI | ES-02 | ES-03, ES-07 | ES-04 | ES-01, ES-08, ES-11 | ES-06 | ES-09, ES-10 | ES-05 |

Cada recurso sigue la secuencia del [contrato de la CLI §6](../../03-contratos/cli.md#6-casos-de-prueba-para-hu-026): crear, consultar, listar, listar parcial, editar, consultar el valor editado, contar, eliminar, y comprobar ERR-03 sobre la clave eliminada y que el conteo vuelve al inicial.

### Criterios de aceptación de HU-026

| Criterio | Evidencia | Resultado |
|---|---|---|
| Seis operaciones por recurso desde REST y CLI en MariaDB y MongoDB | Matriz anterior | 96 de 96 |
| Casos negativos: inválidos, duplicados, relaciones e inexistentes | REST `NE-01` a `NE-23` y CLI `NE-01` a `NE-11`, en cada motor | ERR-01 (cc, sexo, nombre, número, fecha, clave y tamaño de página inválidos), ERR-02 (motor no admitido o ausente), ERR-03 (inexistentes al consultar, editar y eliminar), ERR-04 (duplicados en los cuatro recursos), ERR-05 (dueño, profesión o persona inexistentes), ERR-06 (persona y profesión con dependientes) y ERR-09 (clave del cuerpo distinta de la ruta). Ninguno guarda nada: los conteos vuelven al inicial |
| Cada resultado tiene petición, esperado, obtenido y fecha | Tablas de abajo | Fecha y commit en el encabezado de cada tabla |
| Con tres registros, páginas de tamaño 2 dan 2 y 1 sin repetir; el conteo da 3; tras borrar uno da 2 y el otro motor queda intacto | REST `PG-01` a `PG-11` en cada motor, con los tres teléfonos del DML | Se cumple en los dos sentidos. El teléfono borrado sigue en el otro motor y luego se restaura |

**También quedan verificados:**
- **Selección dinámica:** en REST, cada registro creado en un motor da ERR-03 en el otro (`-03` de cada recurso). En la CLI, `PE-04` y `PE-05` pasan a MongoDB y vuelven a MariaDB en la misma sesión.
- **Persona–Teléfonos (HU-033):** REST `33-01` a `33-03` y CLI `33-01` y `33-02`. Pepe sale con 2 teléfonos y Pepita con la lista vacía; por REST, una persona inexistente da ERR-03.
- **Ceros iniciales:** el teléfono `0088000001` se guarda, se consulta y se lista sin perder los ceros.

### Verificación REST (HU-026)

- Fecha: 2026-11-17 11:52 -0500
- Commit: 92b1454
- URL: http://localhost:3018/api/v1 (con `PERSONAPP_REST_PORT=3018`; por omisión es el puerto 3000)

#### MARIA

Conteos iniciales: persona 5, profesión 2, teléfono 3, estudio 2.

| ID | Caso | Petición | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| MA-PE-01 | Crear persona | `POST /persona?database=MARIA` | 201 con `"dni":"880000001"` | `201 {"dni":"880000001","firstName":"Verificada","lastName":"Hu026","age":"25","sex":"FEMALE","database":"MARIA","s` | OK |
| MA-PR-01 | Crear profesión | `POST /profesion?database=MARIA` | 201 con `"id":880` | `201 {"id":880,"name":"Verificacion","description":"Creada por HU-026"}` | OK |
| MA-TE-01 | Crear teléfono con cero inicial | `POST /telefono?database=MARIA` | 201 con `"number":"0088000001"` | `201 {"number":"0088000001","company":"Claro","ownerId":880000001}` | OK |
| MA-ES-01 | Crear estudio | `POST /estudio?database=MARIA` | 201 con `"universityName":"Javeriana"` | `201 {"professionId":880,"personId":880000001,"graduationDate":"2020-06-15","universityName":"Javeriana"}` | OK |
| MA-PE-02 | Consultar persona | `GET /persona/880000001?database=MARIA` | 200 con `"firstName":"Verificada"` | `200 {"dni":"880000001","firstName":"Verificada","lastName":"Hu026","age":"25","sex":"FEMALE","database":"MARIA","s` | OK |
| MA-PR-02 | Consultar profesión | `GET /profesion/880?database=MARIA` | 200 con `"name":"Verificacion"` | `200 {"id":880,"name":"Verificacion","description":"Creada por HU-026"}` | OK |
| MA-TE-02 | Consultar teléfono | `GET /telefono/0088000001?database=MARIA` | 200 con `"ownerId":880000001` | `200 {"number":"0088000001","company":"Claro","ownerId":880000001}` | OK |
| MA-ES-02 | Consultar estudio | `GET /estudio/880/880000001?database=MARIA` | 200 con `"graduationDate":"2020-06-15"` | `200 {"professionId":880,"personId":880000001,"graduationDate":"2020-06-15","universityName":"Javeriana"}` | OK |
| MA-PE-03 | La persona no existe en MONGO | `GET /persona/880000001?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 880000001 en MONGO"}` | OK |
| MA-PR-03 | La profesión no existe en MONGO | `GET /profesion/880?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una profesión con id 880 en MONGO"}` | OK |
| MA-TE-03 | El teléfono no existe en MONGO | `GET /telefono/0088000001?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un teléfono con número 0088000001 en MONGO"}` | OK |
| MA-ES-03 | El estudio no existe en MONGO | `GET /estudio/880/880000001?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un estudio de la profesión 880 para la persona 880000001 en MONGO"}` | OK |
| MA-PE-04 | Listar personas | `GET /persona?database=MARIA` | 200 con `"dni":"880000001"` | `200 [{"dni":"123456789","firstName":"Pepe","lastName":"Perez","age":"30","sex":"MALE","database":"MARIA","status":` | OK |
| MA-PR-04 | Listar profesiones | `GET /profesion?database=MARIA` | 200 con `"id":880` | `200 [{"id":1,"name":"Ingeniero de Sistemas","description":"Diseño y construcción de software"},{"id":2,"name":"M` | OK |
| MA-TE-04 | Listar teléfonos | `GET /telefono?database=MARIA` | 200 con `"number":"0088000001"` | `200 [{"number":"0088000001","company":"Claro","ownerId":880000001},{"number":"3001234567","company":"Claro","owner` | OK |
| MA-ES-04 | Listar estudios | `GET /estudio?database=MARIA` | 200 con `"professionId":880` | `200 [{"professionId":1,"personId":123456789,"graduationDate":"2015-12-01","universityName":"Pontificia Universidad` | OK |
| MA-PE-05 | Listar parcial de personas | `GET /persona/pagina?database=MARIA&page=0&size=100` | 200 con `"dni":"880000001"` | `200 [{"dni":"123456789","firstName":"Pepe","lastName":"Perez","age":"30","sex":"MALE","database":"MARIA","status":` | OK |
| MA-PR-05 | Listar parcial de profesiones | `GET /profesion/pagina?database=MARIA&page=0&size=100` | 200 con `"id":880` | `200 [{"id":1,"name":"Ingeniero de Sistemas","description":"Diseño y construcción de software"},{"id":2,"name":"M` | OK |
| MA-TE-05 | Listar parcial de teléfonos | `GET /telefono/pagina?database=MARIA&page=0&size=100` | 200 con `"number":"0088000001"` | `200 [{"number":"0088000001","company":"Claro","ownerId":880000001},{"number":"3001234567","company":"Claro","owner` | OK |
| MA-ES-05 | Listar parcial de estudios | `GET /estudio/pagina?database=MARIA&page=0&size=100` | 200 con `"professionId":880` | `200 [{"professionId":1,"personId":123456789,"graduationDate":"2015-12-01","universityName":"Pontificia Universidad` | OK |
| MA-PE-06 | Contar personas | `GET /persona/conteo?database=MARIA` | 200 con `6` | `200 6` | OK |
| MA-PR-06 | Contar profesiones | `GET /profesion/conteo?database=MARIA` | 200 con `3` | `200 3` | OK |
| MA-TE-06 | Contar teléfonos | `GET /telefono/conteo?database=MARIA` | 200 con `4` | `200 4` | OK |
| MA-ES-06 | Contar estudios | `GET /estudio/conteo?database=MARIA` | 200 con `3` | `200 3` | OK |
| MA-PE-07 | Editar persona | `PUT /persona/880000001?database=MARIA` | 200 con `"firstName":"Editada"` | `200 {"dni":"880000001","firstName":"Editada","lastName":"Hu026","age":"26","sex":"FEMALE","database":"MARIA","stat` | OK |
| MA-PE-08 | Consultar la persona editada | `GET /persona/880000001?database=MARIA` | 200 con `"age":"26"` | `200 {"dni":"880000001","firstName":"Editada","lastName":"Hu026","age":"26","sex":"FEMALE","database":"MARIA","stat` | OK |
| MA-PR-07 | Editar profesión | `PUT /profesion/880?database=MARIA` | 200 con `"name":"Verificacion editada"` | `200 {"id":880,"name":"Verificacion editada","description":"Editada por HU-026"}` | OK |
| MA-PR-08 | Consultar la profesión editada | `GET /profesion/880?database=MARIA` | 200 con `Editada por HU-026` | `200 {"id":880,"name":"Verificacion editada","description":"Editada por HU-026"}` | OK |
| MA-TE-07 | Editar teléfono: operador y dueño | `PUT /telefono/0088000001?database=MARIA` | 200 con `"ownerId":123456789` | `200 {"number":"0088000001","company":"Tigo","ownerId":123456789}` | OK |
| MA-TE-08 | Consultar el teléfono editado | `GET /telefono/0088000001?database=MARIA` | 200 con `"company":"Tigo"` | `200 {"number":"0088000001","company":"Tigo","ownerId":123456789}` | OK |
| MA-ES-07 | Editar estudio | `PUT /estudio/880/880000001?database=MARIA` | 200 con `"universityName":"Nacional"` | `200 {"professionId":880,"personId":880000001,"graduationDate":"2021-12-01","universityName":"Nacional"}` | OK |
| MA-ES-08 | Consultar el estudio editado | `GET /estudio/880/880000001?database=MARIA` | 200 con `"graduationDate":"2021-12-01"` | `200 {"professionId":880,"personId":880000001,"graduationDate":"2021-12-01","universityName":"Nacional"}` | OK |
| MA-NE-01 | Persona duplicada | `POST /persona?database=MARIA` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe una persona con cc 880000001 en MARIA"}` | OK |
| MA-NE-02 | Profesión duplicada | `POST /profesion?database=MARIA` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe una profesión con id 880 en MARIA"}` | OK |
| MA-NE-03 | Teléfono duplicado | `POST /telefono?database=MARIA` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe un teléfono con número 0088000001 en MARIA"}` | OK |
| MA-NE-04 | Estudio duplicado | `POST /estudio?database=MARIA` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe un estudio de la profesión 880 para la persona 880000001 en MARIA"}` | OK |
| MA-NE-05 | Persona con cc no numérico | `POST /persona?database=MARIA` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El dni 'abc' no es un número entero válido"}` | OK |
| MA-NE-06 | Persona con sexo no admitido | `POST /persona?database=MARIA` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El sexo 'Z' no es válido; use M o F"}` | OK |
| MA-NE-07 | Profesión sin nombre | `POST /profesion?database=MARIA` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El nombre de la profesión debe tener entre 1 y 90 caracteres"}` | OK |
| MA-NE-08 | Teléfono con letras | `POST /telefono?database=MARIA` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El número debe tener entre 1 y 15 dígitos"}` | OK |
| MA-NE-09 | Estudio con fecha imposible | `POST /estudio?database=MARIA` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"La fecha '2015-13-45' no es válida; use el formato yyyy-MM-dd"}` | OK |
| MA-NE-10 | Teléfono con dueño inexistente | `POST /telefono?database=MARIA` | 422 con `ERR-05` | `422 {"error":"ERR-05","message":"No existe una persona con cc 999 para ser dueña del teléfono en MARIA"}` | OK |
| MA-NE-11 | Estudio con profesión inexistente | `POST /estudio?database=MARIA` | 422 con `ERR-05` | `422 {"error":"ERR-05","message":"No existe una profesión con id 99 en MARIA"}` | OK |
| MA-NE-12 | Estudio con persona inexistente | `POST /estudio?database=MARIA` | 422 con `ERR-05` | `422 {"error":"ERR-05","message":"No existe una persona con cc 999 en MARIA"}` | OK |
| MA-NE-13 | Consultar persona inexistente | `GET /persona/999?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 999 en MARIA"}` | OK |
| MA-NE-14 | Editar profesión inexistente | `PUT /profesion/999?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una profesión con id 999 en MARIA"}` | OK |
| MA-NE-15 | Eliminar teléfono inexistente | `DELETE /telefono/0000000?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un teléfono con número 0000000 en MARIA"}` | OK |
| MA-NE-16 | Consultar estudio inexistente | `GET /estudio/880/147258369?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un estudio de la profesión 880 para la persona 147258369 en MARIA"}` | OK |
| MA-NE-17 | Editar persona con otra cc en el cuerpo | `PUT /persona/880000001?database=MARIA` | 400 con `ERR-09` | `400 {"error":"ERR-09","message":"El cc del cuerpo (880000002) no coincide con el de la ruta (880000001)"}` | OK |
| MA-NE-18 | Eliminar persona con teléfonos y estudios | `DELETE /persona/123456789?database=MARIA` | 409 con `ERR-06` | `409 {"error":"ERR-06","message":"La persona con cc 123456789 tiene teléfonos o estudios asociados en MARIA"}` | OK |
| MA-NE-19 | Eliminar profesión con estudios | `DELETE /profesion/880?database=MARIA` | 409 con `ERR-06` | `409 {"error":"ERR-06","message":"La profesión con id 880 tiene estudios asociados en MARIA"}` | OK |
| MA-NE-20 | Clave con formato inválido | `GET /persona/abc?database=MARIA` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El valor 'abc' no es válido para cc"}` | OK |
| MA-NE-21 | Motor no admitido | `GET /telefono?database=ORACLE` | 400 con `ERR-02` | `400 {"error":"ERR-02","message":"El motor ORACLE no es válido; use MARIA o MONGO"}` | OK |
| MA-NE-22 | Sin motor | `GET /estudio/conteo` | 400 con `ERR-02` | `400 {"error":"ERR-02","message":"Falta el parámetro database; use MARIA o MONGO"}` | OK |
| MA-NE-23 | Tamaño de página fuera de rango | `GET /profesion/pagina?database=MARIA&page=0&size=0` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El tamaño de página debe estar entre 1 y 100"}` | OK |
| MA-ES-09 | Eliminar estudio | `DELETE /estudio/880/880000001?database=MARIA` | 204 | `204 ` | OK |
| MA-ES-10 | Consultar el estudio eliminado | `GET /estudio/880/880000001?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un estudio de la profesión 880 para la persona 880000001 en MARIA"}` | OK |
| MA-TE-09 | Eliminar teléfono | `DELETE /telefono/0088000001?database=MARIA` | 204 | `204 ` | OK |
| MA-TE-10 | Consultar el teléfono eliminado | `GET /telefono/0088000001?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un teléfono con número 0088000001 en MARIA"}` | OK |
| MA-PR-09 | Eliminar profesión | `DELETE /profesion/880?database=MARIA` | 204 | `204 ` | OK |
| MA-PR-10 | Consultar la profesión eliminada | `GET /profesion/880?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una profesión con id 880 en MARIA"}` | OK |
| MA-PE-09 | Eliminar persona | `DELETE /persona/880000001?database=MARIA` | 204 | `204 ` | OK |
| MA-PE-10 | Consultar la persona eliminada | `GET /persona/880000001?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 880000001 en MARIA"}` | OK |
| MA-PE-11 | El conteo de personas vuelve al inicial | `GET /persona/conteo?database=MARIA` | 200 con `5` | `200 5` | OK |
| MA-PR-11 | El conteo de profesiones vuelve al inicial | `GET /profesion/conteo?database=MARIA` | 200 con `2` | `200 2` | OK |
| MA-TE-11 | El conteo de teléfonos vuelve al inicial | `GET /telefono/conteo?database=MARIA` | 200 con `3` | `200 3` | OK |
| MA-ES-11 | El conteo de estudios vuelve al inicial | `GET /estudio/conteo?database=MARIA` | 200 con `2` | `200 2` | OK |
| MA-33-01 | Pepe con sus teléfonos | `GET /persona/123456789/telefonos?database=MARIA` | 200 con 2 elementos | `200 {"dni":"123456789","firstName":"Pepe","lastName":"Perez","age":"30","sex":"MALE","phones":[{"number":"30012345` | OK |
| MA-33-02 | Pepita sin teléfonos | `GET /persona/147258369/telefonos?database=MARIA` | 200 con 0 elementos | `200 {"dni":"147258369","firstName":"Pepita","lastName":"Juarez","age":"10","sex":"FEMALE","phones":[]}` | OK |
| MA-33-03 | Persona inexistente con teléfonos | `GET /persona/999/telefonos?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 999 en MARIA"}` | OK |

#### MONGO

Conteos iniciales: persona 5, profesión 2, teléfono 3, estudio 2.

| ID | Caso | Petición | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| MO-PE-01 | Crear persona | `POST /persona?database=MONGO` | 201 con `"dni":"880000001"` | `201 {"dni":"880000001","firstName":"Verificada","lastName":"Hu026","age":"25","sex":"FEMALE","database":"MONGO","s` | OK |
| MO-PR-01 | Crear profesión | `POST /profesion?database=MONGO` | 201 con `"id":880` | `201 {"id":880,"name":"Verificacion","description":"Creada por HU-026"}` | OK |
| MO-TE-01 | Crear teléfono con cero inicial | `POST /telefono?database=MONGO` | 201 con `"number":"0088000001"` | `201 {"number":"0088000001","company":"Claro","ownerId":880000001}` | OK |
| MO-ES-01 | Crear estudio | `POST /estudio?database=MONGO` | 201 con `"universityName":"Javeriana"` | `201 {"professionId":880,"personId":880000001,"graduationDate":"2020-06-15","universityName":"Javeriana"}` | OK |
| MO-PE-02 | Consultar persona | `GET /persona/880000001?database=MONGO` | 200 con `"firstName":"Verificada"` | `200 {"dni":"880000001","firstName":"Verificada","lastName":"Hu026","age":"25","sex":"FEMALE","database":"MONGO","s` | OK |
| MO-PR-02 | Consultar profesión | `GET /profesion/880?database=MONGO` | 200 con `"name":"Verificacion"` | `200 {"id":880,"name":"Verificacion","description":"Creada por HU-026"}` | OK |
| MO-TE-02 | Consultar teléfono | `GET /telefono/0088000001?database=MONGO` | 200 con `"ownerId":880000001` | `200 {"number":"0088000001","company":"Claro","ownerId":880000001}` | OK |
| MO-ES-02 | Consultar estudio | `GET /estudio/880/880000001?database=MONGO` | 200 con `"graduationDate":"2020-06-15"` | `200 {"professionId":880,"personId":880000001,"graduationDate":"2020-06-15","universityName":"Javeriana"}` | OK |
| MO-PE-03 | La persona no existe en MARIA | `GET /persona/880000001?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 880000001 en MARIA"}` | OK |
| MO-PR-03 | La profesión no existe en MARIA | `GET /profesion/880?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una profesión con id 880 en MARIA"}` | OK |
| MO-TE-03 | El teléfono no existe en MARIA | `GET /telefono/0088000001?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un teléfono con número 0088000001 en MARIA"}` | OK |
| MO-ES-03 | El estudio no existe en MARIA | `GET /estudio/880/880000001?database=MARIA` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un estudio de la profesión 880 para la persona 880000001 en MARIA"}` | OK |
| MO-PE-04 | Listar personas | `GET /persona?database=MONGO` | 200 con `"dni":"880000001"` | `200 [{"dni":"123456789","firstName":"Pepe","lastName":"Perez","age":"30","sex":"MALE","database":"MONGO","status":` | OK |
| MO-PR-04 | Listar profesiones | `GET /profesion?database=MONGO` | 200 con `"id":880` | `200 [{"id":1,"name":"Ingeniero de Sistemas","description":"Diseño y construcción de software"},{"id":2,"name":"M` | OK |
| MO-TE-04 | Listar teléfonos | `GET /telefono?database=MONGO` | 200 con `"number":"0088000001"` | `200 [{"number":"0088000001","company":"Claro","ownerId":880000001},{"number":"3001234567","company":"Claro","owner` | OK |
| MO-ES-04 | Listar estudios | `GET /estudio?database=MONGO` | 200 con `"professionId":880` | `200 [{"professionId":1,"personId":123456789,"graduationDate":"2015-12-01","universityName":"Pontificia Universidad` | OK |
| MO-PE-05 | Listar parcial de personas | `GET /persona/pagina?database=MONGO&page=0&size=100` | 200 con `"dni":"880000001"` | `200 [{"dni":"123456789","firstName":"Pepe","lastName":"Perez","age":"30","sex":"MALE","database":"MONGO","status":` | OK |
| MO-PR-05 | Listar parcial de profesiones | `GET /profesion/pagina?database=MONGO&page=0&size=100` | 200 con `"id":880` | `200 [{"id":1,"name":"Ingeniero de Sistemas","description":"Diseño y construcción de software"},{"id":2,"name":"M` | OK |
| MO-TE-05 | Listar parcial de teléfonos | `GET /telefono/pagina?database=MONGO&page=0&size=100` | 200 con `"number":"0088000001"` | `200 [{"number":"0088000001","company":"Claro","ownerId":880000001},{"number":"3001234567","company":"Claro","owner` | OK |
| MO-ES-05 | Listar parcial de estudios | `GET /estudio/pagina?database=MONGO&page=0&size=100` | 200 con `"professionId":880` | `200 [{"professionId":1,"personId":123456789,"graduationDate":"2015-12-01","universityName":"Pontificia Universidad` | OK |
| MO-PE-06 | Contar personas | `GET /persona/conteo?database=MONGO` | 200 con `6` | `200 6` | OK |
| MO-PR-06 | Contar profesiones | `GET /profesion/conteo?database=MONGO` | 200 con `3` | `200 3` | OK |
| MO-TE-06 | Contar teléfonos | `GET /telefono/conteo?database=MONGO` | 200 con `4` | `200 4` | OK |
| MO-ES-06 | Contar estudios | `GET /estudio/conteo?database=MONGO` | 200 con `3` | `200 3` | OK |
| MO-PE-07 | Editar persona | `PUT /persona/880000001?database=MONGO` | 200 con `"firstName":"Editada"` | `200 {"dni":"880000001","firstName":"Editada","lastName":"Hu026","age":"26","sex":"FEMALE","database":"MONGO","stat` | OK |
| MO-PE-08 | Consultar la persona editada | `GET /persona/880000001?database=MONGO` | 200 con `"age":"26"` | `200 {"dni":"880000001","firstName":"Editada","lastName":"Hu026","age":"26","sex":"FEMALE","database":"MONGO","stat` | OK |
| MO-PR-07 | Editar profesión | `PUT /profesion/880?database=MONGO` | 200 con `"name":"Verificacion editada"` | `200 {"id":880,"name":"Verificacion editada","description":"Editada por HU-026"}` | OK |
| MO-PR-08 | Consultar la profesión editada | `GET /profesion/880?database=MONGO` | 200 con `Editada por HU-026` | `200 {"id":880,"name":"Verificacion editada","description":"Editada por HU-026"}` | OK |
| MO-TE-07 | Editar teléfono: operador y dueño | `PUT /telefono/0088000001?database=MONGO` | 200 con `"ownerId":123456789` | `200 {"number":"0088000001","company":"Tigo","ownerId":123456789}` | OK |
| MO-TE-08 | Consultar el teléfono editado | `GET /telefono/0088000001?database=MONGO` | 200 con `"company":"Tigo"` | `200 {"number":"0088000001","company":"Tigo","ownerId":123456789}` | OK |
| MO-ES-07 | Editar estudio | `PUT /estudio/880/880000001?database=MONGO` | 200 con `"universityName":"Nacional"` | `200 {"professionId":880,"personId":880000001,"graduationDate":"2021-12-01","universityName":"Nacional"}` | OK |
| MO-ES-08 | Consultar el estudio editado | `GET /estudio/880/880000001?database=MONGO` | 200 con `"graduationDate":"2021-12-01"` | `200 {"professionId":880,"personId":880000001,"graduationDate":"2021-12-01","universityName":"Nacional"}` | OK |
| MO-NE-01 | Persona duplicada | `POST /persona?database=MONGO` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe una persona con cc 880000001 en MONGO"}` | OK |
| MO-NE-02 | Profesión duplicada | `POST /profesion?database=MONGO` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe una profesión con id 880 en MONGO"}` | OK |
| MO-NE-03 | Teléfono duplicado | `POST /telefono?database=MONGO` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe un teléfono con número 0088000001 en MONGO"}` | OK |
| MO-NE-04 | Estudio duplicado | `POST /estudio?database=MONGO` | 409 con `ERR-04` | `409 {"error":"ERR-04","message":"Ya existe un estudio de la profesión 880 para la persona 880000001 en MONGO"}` | OK |
| MO-NE-05 | Persona con cc no numérico | `POST /persona?database=MONGO` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El dni 'abc' no es un número entero válido"}` | OK |
| MO-NE-06 | Persona con sexo no admitido | `POST /persona?database=MONGO` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El sexo 'Z' no es válido; use M o F"}` | OK |
| MO-NE-07 | Profesión sin nombre | `POST /profesion?database=MONGO` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El nombre de la profesión debe tener entre 1 y 90 caracteres"}` | OK |
| MO-NE-08 | Teléfono con letras | `POST /telefono?database=MONGO` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El número debe tener entre 1 y 15 dígitos"}` | OK |
| MO-NE-09 | Estudio con fecha imposible | `POST /estudio?database=MONGO` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"La fecha '2015-13-45' no es válida; use el formato yyyy-MM-dd"}` | OK |
| MO-NE-10 | Teléfono con dueño inexistente | `POST /telefono?database=MONGO` | 422 con `ERR-05` | `422 {"error":"ERR-05","message":"No existe una persona con cc 999 para ser dueña del teléfono en MONGO"}` | OK |
| MO-NE-11 | Estudio con profesión inexistente | `POST /estudio?database=MONGO` | 422 con `ERR-05` | `422 {"error":"ERR-05","message":"No existe una profesión con id 99 en MONGO"}` | OK |
| MO-NE-12 | Estudio con persona inexistente | `POST /estudio?database=MONGO` | 422 con `ERR-05` | `422 {"error":"ERR-05","message":"No existe una persona con cc 999 en MONGO"}` | OK |
| MO-NE-13 | Consultar persona inexistente | `GET /persona/999?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 999 en MONGO"}` | OK |
| MO-NE-14 | Editar profesión inexistente | `PUT /profesion/999?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una profesión con id 999 en MONGO"}` | OK |
| MO-NE-15 | Eliminar teléfono inexistente | `DELETE /telefono/0000000?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un teléfono con número 0000000 en MONGO"}` | OK |
| MO-NE-16 | Consultar estudio inexistente | `GET /estudio/880/147258369?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un estudio de la profesión 880 para la persona 147258369 en MONGO"}` | OK |
| MO-NE-17 | Editar persona con otra cc en el cuerpo | `PUT /persona/880000001?database=MONGO` | 400 con `ERR-09` | `400 {"error":"ERR-09","message":"El cc del cuerpo (880000002) no coincide con el de la ruta (880000001)"}` | OK |
| MO-NE-18 | Eliminar persona con teléfonos y estudios | `DELETE /persona/123456789?database=MONGO` | 409 con `ERR-06` | `409 {"error":"ERR-06","message":"La persona con cc 123456789 tiene teléfonos o estudios asociados en MONGO"}` | OK |
| MO-NE-19 | Eliminar profesión con estudios | `DELETE /profesion/880?database=MONGO` | 409 con `ERR-06` | `409 {"error":"ERR-06","message":"La profesión con id 880 tiene estudios asociados en MONGO"}` | OK |
| MO-NE-20 | Clave con formato inválido | `GET /persona/abc?database=MONGO` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El valor 'abc' no es válido para cc"}` | OK |
| MO-NE-21 | Motor no admitido | `GET /telefono?database=ORACLE` | 400 con `ERR-02` | `400 {"error":"ERR-02","message":"El motor ORACLE no es válido; use MARIA o MONGO"}` | OK |
| MO-NE-22 | Sin motor | `GET /estudio/conteo` | 400 con `ERR-02` | `400 {"error":"ERR-02","message":"Falta el parámetro database; use MARIA o MONGO"}` | OK |
| MO-NE-23 | Tamaño de página fuera de rango | `GET /profesion/pagina?database=MONGO&page=0&size=0` | 400 con `ERR-01` | `400 {"error":"ERR-01","message":"El tamaño de página debe estar entre 1 y 100"}` | OK |
| MO-ES-09 | Eliminar estudio | `DELETE /estudio/880/880000001?database=MONGO` | 204 | `204 ` | OK |
| MO-ES-10 | Consultar el estudio eliminado | `GET /estudio/880/880000001?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un estudio de la profesión 880 para la persona 880000001 en MONGO"}` | OK |
| MO-TE-09 | Eliminar teléfono | `DELETE /telefono/0088000001?database=MONGO` | 204 | `204 ` | OK |
| MO-TE-10 | Consultar el teléfono eliminado | `GET /telefono/0088000001?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe un teléfono con número 0088000001 en MONGO"}` | OK |
| MO-PR-09 | Eliminar profesión | `DELETE /profesion/880?database=MONGO` | 204 | `204 ` | OK |
| MO-PR-10 | Consultar la profesión eliminada | `GET /profesion/880?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una profesión con id 880 en MONGO"}` | OK |
| MO-PE-09 | Eliminar persona | `DELETE /persona/880000001?database=MONGO` | 204 | `204 ` | OK |
| MO-PE-10 | Consultar la persona eliminada | `GET /persona/880000001?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 880000001 en MONGO"}` | OK |
| MO-PE-11 | El conteo de personas vuelve al inicial | `GET /persona/conteo?database=MONGO` | 200 con `5` | `200 5` | OK |
| MO-PR-11 | El conteo de profesiones vuelve al inicial | `GET /profesion/conteo?database=MONGO` | 200 con `2` | `200 2` | OK |
| MO-TE-11 | El conteo de teléfonos vuelve al inicial | `GET /telefono/conteo?database=MONGO` | 200 con `3` | `200 3` | OK |
| MO-ES-11 | El conteo de estudios vuelve al inicial | `GET /estudio/conteo?database=MONGO` | 200 con `2` | `200 2` | OK |
| MO-33-01 | Pepe con sus teléfonos | `GET /persona/123456789/telefonos?database=MONGO` | 200 con 2 elementos | `200 {"dni":"123456789","firstName":"Pepe","lastName":"Perez","age":"30","sex":"MALE","phones":[{"number":"30012345` | OK |
| MO-33-02 | Pepita sin teléfonos | `GET /persona/147258369/telefonos?database=MONGO` | 200 con 0 elementos | `200 {"dni":"147258369","firstName":"Pepita","lastName":"Juarez","age":"10","sex":"FEMALE","phones":[]}` | OK |
| MO-33-03 | Persona inexistente con teléfonos | `GET /persona/999/telefonos?database=MONGO` | 404 con `ERR-03` | `404 {"error":"ERR-03","message":"No existe una persona con cc 999 en MONGO"}` | OK |

#### Paginación con tres registros en MARIA

| ID | Caso | Petición | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| MA-PG-01 | Conteo inicial | `GET /telefono/conteo?database=MARIA` | 200 con `3` | `200 3` | OK |
| MA-PG-02 | Página 0 de tamaño 2 | `GET /telefono/pagina?database=MARIA&page=0&size=2` | 200 con 2 elementos | `200 [{"number":"3001234567","company":"Claro","ownerId":123456789},{"number":"3107654321","company":"Movistar","ow` | OK |
| MA-PG-03 | Página 1 de tamaño 2 | `GET /telefono/pagina?database=MARIA&page=1&size=2` | 200 con 1 elementos | `200 [{"number":"3209876543","company":"Tigo","ownerId":321654987}]` | OK |
| MA-PG-04 | Las dos páginas no repiten registros | `GET /telefono/pagina (0 y 1)` | 3 números distintos | `200 3 números distintos` | OK |
| MA-PG-05 | Página 2, fuera de rango | `GET /telefono/pagina?database=MARIA&page=2&size=2` | 200 con 0 elementos | `200 []` | OK |
| MA-PG-06 | Eliminar un teléfono | `DELETE /telefono/3107654321?database=MARIA` | 204 | `204 ` | OK |
| MA-PG-07 | El conteo baja a dos | `GET /telefono/conteo?database=MARIA` | 200 con `2` | `200 2` | OK |
| MA-PG-08 | El otro motor sigue con tres | `GET /telefono/conteo?database=MONGO` | 200 con `3` | `200 3` | OK |
| MA-PG-09 | El teléfono sigue en el otro motor | `GET /telefono/3107654321?database=MONGO` | 200 con `"ownerId":123456789` | `200 {"number":"3107654321","company":"Movistar","ownerId":123456789}` | OK |
| MA-PG-10 | Restaurar el teléfono del DML | `POST /telefono?database=MARIA` | 201 | `201 {"number":"3107654321","company":"Movistar","ownerId":123456789}` | OK |
| MA-PG-11 | El conteo vuelve a tres | `GET /telefono/conteo?database=MARIA` | 200 con `3` | `200 3` | OK |

#### Paginación con tres registros en MONGO

| ID | Caso | Petición | Esperado | Obtenido | Estado |
|---|---|---|---|---|---|
| MO-PG-01 | Conteo inicial | `GET /telefono/conteo?database=MONGO` | 200 con `3` | `200 3` | OK |
| MO-PG-02 | Página 0 de tamaño 2 | `GET /telefono/pagina?database=MONGO&page=0&size=2` | 200 con 2 elementos | `200 [{"number":"3001234567","company":"Claro","ownerId":123456789},{"number":"3107654321","company":"Movistar","ow` | OK |
| MO-PG-03 | Página 1 de tamaño 2 | `GET /telefono/pagina?database=MONGO&page=1&size=2` | 200 con 1 elementos | `200 [{"number":"3209876543","company":"Tigo","ownerId":321654987}]` | OK |
| MO-PG-04 | Las dos páginas no repiten registros | `GET /telefono/pagina (0 y 1)` | 3 números distintos | `200 3 números distintos` | OK |
| MO-PG-05 | Página 2, fuera de rango | `GET /telefono/pagina?database=MONGO&page=2&size=2` | 200 con 0 elementos | `200 []` | OK |
| MO-PG-06 | Eliminar un teléfono | `DELETE /telefono/3107654321?database=MONGO` | 204 | `204 ` | OK |
| MO-PG-07 | El conteo baja a dos | `GET /telefono/conteo?database=MONGO` | 200 con `2` | `200 2` | OK |
| MO-PG-08 | El otro motor sigue con tres | `GET /telefono/conteo?database=MARIA` | 200 con `3` | `200 3` | OK |
| MO-PG-09 | El teléfono sigue en el otro motor | `GET /telefono/3107654321?database=MARIA` | 200 con `"ownerId":123456789` | `200 {"number":"3107654321","company":"Movistar","ownerId":123456789}` | OK |
| MO-PG-10 | Restaurar el teléfono del DML | `POST /telefono?database=MONGO` | 201 | `201 {"number":"3107654321","company":"Movistar","ownerId":123456789}` | OK |
| MO-PG-11 | El conteo vuelve a tres | `GET /telefono/conteo?database=MONGO` | 200 con `3` | `200 3` | OK |

**Resultado:** 162 de 162 comprobaciones correctas, 0 fallas.

### Verificación CLI (HU-026)

- Fecha: 2026-11-17 11:51 -0500
- Commit: 92b1454
- Comando: `docker compose --env-file <archivo de entorno> run -T --rm --build cli`, una sola sesión; código de salida 0

| ID | Caso | Texto esperado en la salida, en orden | Estado |
|---|---|---|---|
| GE-01 | Motor no válido en el menú | `[ERR-02] Opción de motor no válida` | OK |
| MA-PE-01 | Contar personas | `Total en MARIA: 5` | OK |
| MA-PE-02 | Crear persona | `Creada:` | OK |
| MA-PE-03 | Consultar persona | `Verificada` | OK |
| MA-PE-04 | Cambiar a MONGO sin reiniciar: la persona no existe allí | `[ERR-03] No existe una persona con cc 880000001 en MONGO` | OK |
| MA-PE-05 | Volver a MARIA: la persona sigue ahí | `Hu026` | OK |
| MA-PE-06 | Listar personas | `880000001` | OK |
| MA-PE-07 | Listar parcial de personas | `Página 0 · 6 registros` | OK |
| MA-PE-08 | Editar persona: muestra el registro actual | `Actual:` | OK |
| MA-PE-09 | Editar persona | `Editada:` | OK |
| MA-PE-10 | Consultar la persona editada | `Editada` | OK |
| MA-PE-11 | Contar tras crear | `Total en MARIA: 6` | OK |
| MA-33-01 | Pepe con sus teléfonos (HU-033) | `Teléfonos en MARIA: 2` | OK |
| MA-33-02 | Pepita sin teléfonos (HU-033) | `(sin teléfonos)` | OK |
| MA-NE-01 | Persona inexistente | `[ERR-03] No existe una persona con cc 999 en MARIA` | OK |
| MA-NE-02 | Persona duplicada | `[ERR-04]` | OK |
| MA-NE-03 | Eliminar persona con dependientes | `[ERR-06]` | OK |
| MA-NE-04 | Opción no numérica: se vuelve a pedir | `[ERR-01] 'abc' no es un número entero` | OK |
| MA-PR-01 | Contar profesiones | `Total en MARIA: 2` | OK |
| MA-PR-02 | Crear profesión | `Creada:` | OK |
| MA-PR-03 | Consultar profesión | `Creada por HU-026` | OK |
| MA-PR-04 | Listar profesiones | `880` | OK |
| MA-PR-05 | Listar parcial de profesiones | `Página 0 · 3 registros` | OK |
| MA-PR-06 | Editar profesión | `Editada:` | OK |
| MA-PR-07 | Consultar la profesión editada | `Editada por HU-026` | OK |
| MA-PR-08 | Contar tras crear | `Total en MARIA: 3` | OK |
| MA-NE-05 | Profesión sin nombre | `[ERR-01]` | OK |
| MA-NE-06 | Eliminar profesión con estudios | `[ERR-06]` | OK |
| MA-TE-01 | Contar teléfonos | `Total en MARIA: 3` | OK |
| MA-TE-02 | Crear teléfono con cero inicial | `Creado:` | OK |
| MA-TE-03 | Consultar teléfono | `0088000001` | OK |
| MA-TE-04 | Listar teléfonos | `3209876543` | OK |
| MA-TE-05 | Página 0 de tamaño 2 | `Página 0 · 2 registros` | OK |
| MA-TE-06 | Página 1 de tamaño 2 | `Página 1 · 2 registros` | OK |
| MA-TE-07 | Editar teléfono | `Editado:` | OK |
| MA-TE-08 | Consultar el teléfono editado | `Tigo` | OK |
| MA-TE-09 | Contar tras crear | `Total en MARIA: 4` | OK |
| MA-NE-07 | Teléfono con dueño inexistente | `[ERR-05]` | OK |
| MA-NE-08 | Teléfono duplicado | `[ERR-04]` | OK |
| MA-ES-01 | Contar estudios | `Total en MARIA: 2` | OK |
| MA-ES-02 | Crear estudio | `Creado:` | OK |
| MA-ES-03 | Consultar estudio | `Javeriana` | OK |
| MA-ES-04 | Listar estudios | `880` | OK |
| MA-ES-05 | Listar parcial de estudios | `Página 0 · 3 registros` | OK |
| MA-NE-09 | Fecha imposible: se vuelve a pedir | `[ERR-01] La fecha '2015-13-45' no es válida` | OK |
| MA-ES-06 | Editar estudio | `Editado:` | OK |
| MA-ES-07 | Consultar el estudio editado | `Nacional` | OK |
| MA-ES-08 | Contar tras crear | `Total en MARIA: 3` | OK |
| MA-NE-10 | Estudio con profesión inexistente | `[ERR-05] No existe una profesión con id 99 en MARIA` | OK |
| MA-NE-11 | Estudio duplicado | `[ERR-04]` | OK |
| MA-ES-09 | Eliminar estudio | `Eliminado el estudio (880, 880000001) en MARIA` | OK |
| MA-ES-10 | Consultar el estudio eliminado | `[ERR-03]` | OK |
| MA-ES-11 | El conteo vuelve al inicial | `Total en MARIA: 2` | OK |
| MA-TE-10 | Eliminar teléfono | `Eliminado el teléfono 0088000001 en MARIA` | OK |
| MA-TE-11 | Consultar el teléfono eliminado | `[ERR-03]` | OK |
| MA-TE-12 | El conteo vuelve al inicial | `Total en MARIA: 3` | OK |
| MA-PR-09 | Eliminar profesión | `Eliminada la profesión con id 880 en MARIA` | OK |
| MA-PR-10 | Consultar la profesión eliminada | `[ERR-03]` | OK |
| MA-PR-11 | El conteo vuelve al inicial | `Total en MARIA: 2` | OK |
| MA-PE-12 | Eliminar persona | `Eliminada la persona con cc 880000001 en MARIA` | OK |
| MA-PE-13 | Consultar la persona eliminada | `[ERR-03]` | OK |
| MA-PE-14 | El conteo vuelve al inicial | `Total en MARIA: 5` | OK |
| MO-PE-01 | Contar personas | `Total en MONGO: 5` | OK |
| MO-PE-02 | Crear persona | `Creada:` | OK |
| MO-PE-03 | Consultar persona | `Verificada` | OK |
| MO-PE-04 | Cambiar a MARIA sin reiniciar: la persona no existe allí | `[ERR-03] No existe una persona con cc 880000001 en MARIA` | OK |
| MO-PE-05 | Volver a MONGO: la persona sigue ahí | `Hu026` | OK |
| MO-PE-06 | Listar personas | `880000001` | OK |
| MO-PE-07 | Listar parcial de personas | `Página 0 · 6 registros` | OK |
| MO-PE-08 | Editar persona: muestra el registro actual | `Actual:` | OK |
| MO-PE-09 | Editar persona | `Editada:` | OK |
| MO-PE-10 | Consultar la persona editada | `Editada` | OK |
| MO-PE-11 | Contar tras crear | `Total en MONGO: 6` | OK |
| MO-33-01 | Pepe con sus teléfonos (HU-033) | `Teléfonos en MONGO: 2` | OK |
| MO-33-02 | Pepita sin teléfonos (HU-033) | `(sin teléfonos)` | OK |
| MO-NE-01 | Persona inexistente | `[ERR-03] No existe una persona con cc 999 en MONGO` | OK |
| MO-NE-02 | Persona duplicada | `[ERR-04]` | OK |
| MO-NE-03 | Eliminar persona con dependientes | `[ERR-06]` | OK |
| MO-NE-04 | Opción no numérica: se vuelve a pedir | `[ERR-01] 'abc' no es un número entero` | OK |
| MO-PR-01 | Contar profesiones | `Total en MONGO: 2` | OK |
| MO-PR-02 | Crear profesión | `Creada:` | OK |
| MO-PR-03 | Consultar profesión | `Creada por HU-026` | OK |
| MO-PR-04 | Listar profesiones | `880` | OK |
| MO-PR-05 | Listar parcial de profesiones | `Página 0 · 3 registros` | OK |
| MO-PR-06 | Editar profesión | `Editada:` | OK |
| MO-PR-07 | Consultar la profesión editada | `Editada por HU-026` | OK |
| MO-PR-08 | Contar tras crear | `Total en MONGO: 3` | OK |
| MO-NE-05 | Profesión sin nombre | `[ERR-01]` | OK |
| MO-NE-06 | Eliminar profesión con estudios | `[ERR-06]` | OK |
| MO-TE-01 | Contar teléfonos | `Total en MONGO: 3` | OK |
| MO-TE-02 | Crear teléfono con cero inicial | `Creado:` | OK |
| MO-TE-03 | Consultar teléfono | `0088000001` | OK |
| MO-TE-04 | Listar teléfonos | `3209876543` | OK |
| MO-TE-05 | Página 0 de tamaño 2 | `Página 0 · 2 registros` | OK |
| MO-TE-06 | Página 1 de tamaño 2 | `Página 1 · 2 registros` | OK |
| MO-TE-07 | Editar teléfono | `Editado:` | OK |
| MO-TE-08 | Consultar el teléfono editado | `Tigo` | OK |
| MO-TE-09 | Contar tras crear | `Total en MONGO: 4` | OK |
| MO-NE-07 | Teléfono con dueño inexistente | `[ERR-05]` | OK |
| MO-NE-08 | Teléfono duplicado | `[ERR-04]` | OK |
| MO-ES-01 | Contar estudios | `Total en MONGO: 2` | OK |
| MO-ES-02 | Crear estudio | `Creado:` | OK |
| MO-ES-03 | Consultar estudio | `Javeriana` | OK |
| MO-ES-04 | Listar estudios | `880` | OK |
| MO-ES-05 | Listar parcial de estudios | `Página 0 · 3 registros` | OK |
| MO-NE-09 | Fecha imposible: se vuelve a pedir | `[ERR-01] La fecha '2015-13-45' no es válida` | OK |
| MO-ES-06 | Editar estudio | `Editado:` | OK |
| MO-ES-07 | Consultar el estudio editado | `Nacional` | OK |
| MO-ES-08 | Contar tras crear | `Total en MONGO: 3` | OK |
| MO-NE-10 | Estudio con profesión inexistente | `[ERR-05] No existe una profesión con id 99 en MONGO` | OK |
| MO-NE-11 | Estudio duplicado | `[ERR-04]` | OK |
| MO-ES-09 | Eliminar estudio | `Eliminado el estudio (880, 880000001) en MONGO` | OK |
| MO-ES-10 | Consultar el estudio eliminado | `[ERR-03]` | OK |
| MO-ES-11 | El conteo vuelve al inicial | `Total en MONGO: 2` | OK |
| MO-TE-10 | Eliminar teléfono | `Eliminado el teléfono 0088000001 en MONGO` | OK |
| MO-TE-11 | Consultar el teléfono eliminado | `[ERR-03]` | OK |
| MO-TE-12 | El conteo vuelve al inicial | `Total en MONGO: 3` | OK |
| MO-PR-09 | Eliminar profesión | `Eliminada la profesión con id 880 en MONGO` | OK |
| MO-PR-10 | Consultar la profesión eliminada | `[ERR-03]` | OK |
| MO-PR-11 | El conteo vuelve al inicial | `Total en MONGO: 2` | OK |
| MO-PE-12 | Eliminar persona | `Eliminada la persona con cc 880000001 en MONGO` | OK |
| MO-PE-13 | Consultar la persona eliminada | `[ERR-03]` | OK |
| MO-PE-14 | El conteo vuelve al inicial | `Total en MONGO: 5` | OK |

**Resultado:** 123 de 123 comprobaciones correctas, 0 fallas. La salida completa de la sesión tiene 1247 líneas; el guion la deja en `hu026-cli-salida.txt`.
