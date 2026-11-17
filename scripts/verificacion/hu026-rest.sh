#!/usr/bin/env bash
# HU-026 · Verificación REST de las seis operaciones, el listado parcial y la consulta
# Persona–Teléfonos en los dos motores, con casos negativos.
#
# Uso: scripts/verificacion/hu026-rest.sh [URL base]   (por omisión http://localhost:3000)
# Requisitos: bash, curl y python (solo para contar elementos de un arreglo JSON).
# Parte de las bases con los DML del repositorio. Crea registros propios (cc 880000001,
# profesión 880, teléfono 0088000001 y estudio (880, 880000001)), los borra al final y
# restaura el teléfono que borra en la prueba de paginación, así que las bases quedan
# como estaban. Escribe una tabla Markdown en la salida estándar y termina con código 1
# si alguna comprobación falla.

set -u
BASE="${1:-http://localhost:3000}/api/v1"
PY="$(command -v python3 || command -v python)"
TOTAL=0
FALLAS=0
CODE=""
BODY=""

req() { # método ruta [cuerpo]
	local salida
	if [ $# -ge 3 ]; then
		salida="$(curl -s -w '\n%{http_code}' -X "$1" -H 'Content-Type: application/json' -d "$3" "$BASE$2")"
	else
		salida="$(curl -s -w '\n%{http_code}' -X "$1" "$BASE$2")"
	fi
	CODE="${salida##*$'\n'}"
	BODY="${salida%$'\n'*}"
	[ "$BODY" = "$CODE" ] && BODY=""
}

jlen() { # longitud del arreglo JSON de BODY, o del campo indicado
	printf '%s' "$BODY" | "$PY" -c "import json,sys; d=json.load(sys.stdin); print(len(d['$1'] if '$1' else d))" 2>/dev/null
}

fila() { # id descripción método ruta esperado ok
	TOTAL=$((TOTAL + 1))
	local estado="OK"
	if [ "$6" != "1" ]; then estado="**FALLA**"; FALLAS=$((FALLAS + 1)); fi
	local obtenido="$CODE ${BODY:0:110}"
	obtenido="${obtenido//|/\\|}"
	printf '| %s | %s | `%s %s` | %s | `%s` | %s |\n' "$1" "$2" "$3" "${4//|/\\|}" "$5" "$obtenido" "$estado"
}

# Ejecuta una petición y compara el código y, si se da, un texto que debe aparecer en el cuerpo.
check() { # id descripción método ruta cuerpo|- código [texto]
	if [ "$5" = "-" ]; then req "$3" "$4"; else req "$3" "$4" "$5"; fi
	local ok=1
	[ "$CODE" = "$6" ] || ok=0
	if [ $# -ge 7 ] && [[ "$BODY" != *"$7"* ]]; then ok=0; fi
	local esperado="$6"
	[ $# -ge 7 ] && esperado="$6 con \`${7//|/\\|}\`"
	fila "$1" "$2" "$3" "$4" "$esperado" "$ok"
}

# Igual que check, pero el cuerpo debe ser exactamente el valor dado (conteos).
check_igual() { # id descripción ruta valor
	req GET "$3"
	local ok=0
	[ "$CODE" = "200" ] && [ "$BODY" = "$4" ] && ok=1
	fila "$1" "$2" GET "$3" "200 con \`$4\`" "$ok"
}

# El arreglo (o el campo dado) tiene exactamente n elementos.
check_largo() { # id descripción ruta n [campo]
	req GET "$3"
	local n
	n="$(jlen "${5:-}")"
	local ok=0
	[ "$CODE" = "200" ] && [ "$n" = "$4" ] && ok=1
	fila "$1" "$2" GET "$3" "200 con $4 elementos" "$ok"
}

conteo() { req GET "/$1/conteo?database=$2"; printf '%s' "$BODY"; }

PERSONA='{"dni":"880000001","firstName":"Verificada","lastName":"Hu026","age":"25","sex":"FEMALE"}'
PERSONA_ED='{"dni":"880000001","firstName":"Editada","lastName":"Hu026","age":"26","sex":"FEMALE"}'
PROFESION='{"id":880,"name":"Verificacion","description":"Creada por HU-026"}'
PROFESION_ED='{"id":880,"name":"Verificacion editada","description":"Editada por HU-026"}'
TELEFONO='{"number":"0088000001","company":"Claro","ownerId":880000001}'
TELEFONO_ED='{"number":"0088000001","company":"Tigo","ownerId":123456789}'
ESTUDIO='{"professionId":880,"personId":880000001,"graduationDate":"2020-06-15","universityName":"Javeriana"}'
ESTUDIO_ED='{"professionId":880,"personId":880000001,"graduationDate":"2021-12-01","universityName":"Nacional"}'

recorrer() { # motor otro_motor
	local db="$1" otro="$2" q="?database=$1"
	local np nf nt ne
	np="$(conteo persona "$db")"; nf="$(conteo profesion "$db")"
	nt="$(conteo telefono "$db")"; ne="$(conteo estudio "$db")"
	local m="${db:0:2}"

	echo
	echo "### $db"
	echo
	echo "Conteos iniciales: persona $np, profesión $nf, teléfono $nt, estudio $ne."
	echo
	echo '| ID | Caso | Petición | Esperado | Obtenido | Estado |'
	echo '|---|---|---|---|---|---|'

	# Crear (las referencias de teléfono y estudio son los registros recién creados)
	check "$m-PE-01" "Crear persona" POST "/persona$q" "$PERSONA" 201 '"dni":"880000001"'
	check "$m-PR-01" "Crear profesión" POST "/profesion$q" "$PROFESION" 201 '"id":880'
	check "$m-TE-01" "Crear teléfono con cero inicial" POST "/telefono$q" "$TELEFONO" 201 '"number":"0088000001"'
	check "$m-ES-01" "Crear estudio" POST "/estudio$q" "$ESTUDIO" 201 '"universityName":"Javeriana"'

	# Consultar por identificador
	check "$m-PE-02" "Consultar persona" GET "/persona/880000001$q" - 200 '"firstName":"Verificada"'
	check "$m-PR-02" "Consultar profesión" GET "/profesion/880$q" - 200 '"name":"Verificacion"'
	check "$m-TE-02" "Consultar teléfono" GET "/telefono/0088000001$q" - 200 '"ownerId":880000001'
	check "$m-ES-02" "Consultar estudio" GET "/estudio/880/880000001$q" - 200 '"graduationDate":"2020-06-15"'

	# El otro motor no ve lo creado (C-03)
	check "$m-PE-03" "La persona no existe en $otro" GET "/persona/880000001?database=$otro" - 404 ERR-03
	check "$m-PR-03" "La profesión no existe en $otro" GET "/profesion/880?database=$otro" - 404 ERR-03
	check "$m-TE-03" "El teléfono no existe en $otro" GET "/telefono/0088000001?database=$otro" - 404 ERR-03
	check "$m-ES-03" "El estudio no existe en $otro" GET "/estudio/880/880000001?database=$otro" - 404 ERR-03

	# Listar todos y listar parcial
	check "$m-PE-04" "Listar personas" GET "/persona$q" - 200 '"dni":"880000001"'
	check "$m-PR-04" "Listar profesiones" GET "/profesion$q" - 200 '"id":880'
	check "$m-TE-04" "Listar teléfonos" GET "/telefono$q" - 200 '"number":"0088000001"'
	check "$m-ES-04" "Listar estudios" GET "/estudio$q" - 200 '"professionId":880'
	check "$m-PE-05" "Listar parcial de personas" GET "/persona/pagina$q&page=0&size=100" - 200 '"dni":"880000001"'
	check "$m-PR-05" "Listar parcial de profesiones" GET "/profesion/pagina$q&page=0&size=100" - 200 '"id":880'
	check "$m-TE-05" "Listar parcial de teléfonos" GET "/telefono/pagina$q&page=0&size=100" - 200 '"number":"0088000001"'
	check "$m-ES-05" "Listar parcial de estudios" GET "/estudio/pagina$q&page=0&size=100" - 200 '"professionId":880'

	# Contar
	check_igual "$m-PE-06" "Contar personas" "/persona/conteo$q" "$((np + 1))"
	check_igual "$m-PR-06" "Contar profesiones" "/profesion/conteo$q" "$((nf + 1))"
	check_igual "$m-TE-06" "Contar teléfonos" "/telefono/conteo$q" "$((nt + 1))"
	check_igual "$m-ES-06" "Contar estudios" "/estudio/conteo$q" "$((ne + 1))"

	# Editar y comprobar el valor editado
	check "$m-PE-07" "Editar persona" PUT "/persona/880000001$q" "$PERSONA_ED" 200 '"firstName":"Editada"'
	check "$m-PE-08" "Consultar la persona editada" GET "/persona/880000001$q" - 200 '"age":"26"'
	check "$m-PR-07" "Editar profesión" PUT "/profesion/880$q" "$PROFESION_ED" 200 '"name":"Verificacion editada"'
	check "$m-PR-08" "Consultar la profesión editada" GET "/profesion/880$q" - 200 'Editada por HU-026'
	check "$m-TE-07" "Editar teléfono: operador y dueño" PUT "/telefono/0088000001$q" "$TELEFONO_ED" 200 '"ownerId":123456789'
	check "$m-TE-08" "Consultar el teléfono editado" GET "/telefono/0088000001$q" - 200 '"company":"Tigo"'
	check "$m-ES-07" "Editar estudio" PUT "/estudio/880/880000001$q" "$ESTUDIO_ED" 200 '"universityName":"Nacional"'
	check "$m-ES-08" "Consultar el estudio editado" GET "/estudio/880/880000001$q" - 200 '"graduationDate":"2021-12-01"'

	# Casos negativos: inválidos, duplicados, relaciones, inexistentes, clave distinta, dependientes, motor
	check "$m-NE-01" "Persona duplicada" POST "/persona$q" "$PERSONA" 409 ERR-04
	check "$m-NE-02" "Profesión duplicada" POST "/profesion$q" "$PROFESION" 409 ERR-04
	check "$m-NE-03" "Teléfono duplicado" POST "/telefono$q" "$TELEFONO" 409 ERR-04
	check "$m-NE-04" "Estudio duplicado" POST "/estudio$q" "$ESTUDIO" 409 ERR-04
	check "$m-NE-05" "Persona con cc no numérico" POST "/persona$q" '{"dni":"abc","firstName":"X","lastName":"Y","sex":"MALE"}' 400 ERR-01
	check "$m-NE-06" "Persona con sexo no admitido" POST "/persona$q" '{"dni":"880000002","firstName":"X","lastName":"Y","sex":"Z"}' 400 ERR-01
	check "$m-NE-07" "Profesión sin nombre" POST "/profesion$q" '{"id":881}' 400 ERR-01
	check "$m-NE-08" "Teléfono con letras" POST "/telefono$q" '{"number":"300-ABC","company":"Claro","ownerId":123456789}' 400 ERR-01
	check "$m-NE-09" "Estudio con fecha imposible" POST "/estudio$q" '{"professionId":1,"personId":147258369,"graduationDate":"2015-13-45"}' 400 ERR-01
	check "$m-NE-10" "Teléfono con dueño inexistente" POST "/telefono$q" '{"number":"0088000002","company":"Claro","ownerId":999}' 422 ERR-05
	check "$m-NE-11" "Estudio con profesión inexistente" POST "/estudio$q" '{"professionId":99,"personId":147258369}' 422 ERR-05
	check "$m-NE-12" "Estudio con persona inexistente" POST "/estudio$q" '{"professionId":1,"personId":999}' 422 ERR-05
	check "$m-NE-13" "Consultar persona inexistente" GET "/persona/999$q" - 404 ERR-03
	check "$m-NE-14" "Editar profesión inexistente" PUT "/profesion/999$q" '{"id":999,"name":"X"}' 404 ERR-03
	check "$m-NE-15" "Eliminar teléfono inexistente" DELETE "/telefono/0000000$q" - 404 ERR-03
	check "$m-NE-16" "Consultar estudio inexistente" GET "/estudio/880/147258369$q" - 404 ERR-03
	check "$m-NE-17" "Editar persona con otra cc en el cuerpo" PUT "/persona/880000001$q" '{"dni":"880000002","firstName":"X","lastName":"Y","sex":"MALE"}' 400 ERR-09
	check "$m-NE-18" "Eliminar persona con teléfonos y estudios" DELETE "/persona/123456789$q" - 409 ERR-06
	check "$m-NE-19" "Eliminar profesión con estudios" DELETE "/profesion/880$q" - 409 ERR-06
	check "$m-NE-20" "Clave con formato inválido" GET "/persona/abc$q" - 400 ERR-01
	check "$m-NE-21" "Motor no admitido" GET "/telefono?database=ORACLE" - 400 ERR-02
	check "$m-NE-22" "Sin motor" GET "/estudio/conteo" - 400 ERR-02
	check "$m-NE-23" "Tamaño de página fuera de rango" GET "/profesion/pagina$q&page=0&size=0" - 400 ERR-01

	# Eliminar en orden inverso a las dependencias, y comprobar ERR-03 y el conteo
	check "$m-ES-09" "Eliminar estudio" DELETE "/estudio/880/880000001$q" - 204
	check "$m-ES-10" "Consultar el estudio eliminado" GET "/estudio/880/880000001$q" - 404 ERR-03
	check "$m-TE-09" "Eliminar teléfono" DELETE "/telefono/0088000001$q" - 204
	check "$m-TE-10" "Consultar el teléfono eliminado" GET "/telefono/0088000001$q" - 404 ERR-03
	check "$m-PR-09" "Eliminar profesión" DELETE "/profesion/880$q" - 204
	check "$m-PR-10" "Consultar la profesión eliminada" GET "/profesion/880$q" - 404 ERR-03
	check "$m-PE-09" "Eliminar persona" DELETE "/persona/880000001$q" - 204
	check "$m-PE-10" "Consultar la persona eliminada" GET "/persona/880000001$q" - 404 ERR-03
	check_igual "$m-PE-11" "El conteo de personas vuelve al inicial" "/persona/conteo$q" "$np"
	check_igual "$m-PR-11" "El conteo de profesiones vuelve al inicial" "/profesion/conteo$q" "$nf"
	check_igual "$m-TE-11" "El conteo de teléfonos vuelve al inicial" "/telefono/conteo$q" "$nt"
	check_igual "$m-ES-11" "El conteo de estudios vuelve al inicial" "/estudio/conteo$q" "$ne"

	# Persona con sus teléfonos (HU-033)
	check_largo "$m-33-01" "Pepe con sus teléfonos" "/persona/123456789/telefonos$q" 2 phones
	check_largo "$m-33-02" "Pepita sin teléfonos" "/persona/147258369/telefonos$q" 0 phones
	check "$m-33-03" "Persona inexistente con teléfonos" GET "/persona/999/telefonos$q" - 404 ERR-03
}

# Paginación con tres registros: los tres teléfonos del DML.
paginacion() { # motor otro_motor
	local db="$1" otro="$2" q="?database=$1" m="${1:0:2}"
	echo
	echo "### Paginación con tres registros en $db"
	echo
	echo '| ID | Caso | Petición | Esperado | Obtenido | Estado |'
	echo '|---|---|---|---|---|---|'
	check_igual "$m-PG-01" "Conteo inicial" "/telefono/conteo$q" 3
	check_largo "$m-PG-02" "Página 0 de tamaño 2" "/telefono/pagina$q&page=0&size=2" 2
	local p0="$BODY"
	check_largo "$m-PG-03" "Página 1 de tamaño 2" "/telefono/pagina$q&page=1&size=2" 1
	local p1="$BODY"
	local distintos
	distintos="$(printf '%s\n%s' "$p0" "$p1" | "$PY" -c "import json,sys; a,b=sys.stdin.read().split('\n',1); s=[t['number'] for t in json.loads(a)+json.loads(b)]; print(len(set(s)))")"
	CODE=200; BODY="$distintos números distintos"
	fila "$m-PG-04" "Las dos páginas no repiten registros" GET "/telefono/pagina (0 y 1)" "3 números distintos" "$([ "$distintos" = 3 ] && echo 1)"
	check_largo "$m-PG-05" "Página 2, fuera de rango" "/telefono/pagina$q&page=2&size=2" 0
	check "$m-PG-06" "Eliminar un teléfono" DELETE "/telefono/3107654321$q" - 204
	check_igual "$m-PG-07" "El conteo baja a dos" "/telefono/conteo$q" 2
	check_igual "$m-PG-08" "El otro motor sigue con tres" "/telefono/conteo?database=$otro" 3
	check "$m-PG-09" "El teléfono sigue en el otro motor" GET "/telefono/3107654321?database=$otro" - 200 '"ownerId":123456789'
	check "$m-PG-10" "Restaurar el teléfono del DML" POST "/telefono$q" '{"number":"3107654321","company":"Movistar","ownerId":123456789}' 201
	check_igual "$m-PG-11" "El conteo vuelve a tres" "/telefono/conteo$q" 3
}

echo "## Verificación REST (HU-026)"
echo
echo "- Fecha: $(date '+%Y-%m-%d %H:%M %z')"
echo "- Commit: $(git rev-parse --short HEAD 2>/dev/null || echo 'sin repositorio')"
echo "- URL: $BASE"
recorrer MARIA MONGO
recorrer MONGO MARIA
paginacion MARIA MONGO
paginacion MONGO MARIA
echo
echo "**Resultado:** $((TOTAL - FALLAS)) de $TOTAL comprobaciones correctas, $FALLAS fallas."
[ "$FALLAS" -eq 0 ]
