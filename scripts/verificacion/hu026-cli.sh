#!/usr/bin/env bash
# HU-026 · Verificación de la CLI: las seis operaciones y el listado parcial de los cuatro
# recursos, la consulta Persona–Teléfonos y casos negativos, en MariaDB y en MongoDB, todo en
# UNA sola sesión que cambia de motor desde el menú sin reiniciar la aplicación.
#
# Uso:   scripts/verificacion/hu026-cli.sh [comando que abre la CLI]
#        Por omisión: docker compose run -T --rm --build cli
#        Sin contenedor: scripts/verificacion/hu026-cli.sh "java -jar cli-input-adapter/target/cli-input-adapter-0.0.1-SNAPSHOT-exec.jar"
# Parte de las bases con los DML. Usa los mismos registros propios que hu026-rest.sh y los
# borra al final, así que las bases quedan como estaban.
# Genera la entrada de la sesión, la ejecuta y busca, en orden, cada texto esperado en la
# salida. Escribe una tabla Markdown y deja la salida completa en hu026-cli-salida.txt
# (o en la ruta de la variable SALIDA). Termina con código 1 si alguna comprobación falla.

set -u
CLI="${1:-docker compose run -T --rm --build cli}"
SALIDA="${SALIDA:-hu026-cli-salida.txt}"
ENTRADA=""
ESPERADOS=()

entra() { local x; for x in "$@"; do ENTRADA+="$x"$'\n'; done; }
espera() { ESPERADOS+=("$1|$2|$3"); } # id|caso|texto

# Recorre los cuatro recursos en un motor. $1 = número del motor en el menú, $2 = nombre,
# $3 = número del otro motor, $4 = nombre del otro motor.
bloque() {
	local n="$1" db="$2" on="$3" otro="$4" m="${2:0:2}"

	# Persona
	entra 1 "$n"
	entra 3;                                                    espera "$m-PE-01" "Contar personas" "Total en $db: 5"
	entra 4 880000001 Verificada Hu026 F 25;                    espera "$m-PE-02" "Crear persona" "Creada:"
	entra 1 880000001;                                          espera "$m-PE-03" "Consultar persona" "Verificada"
	entra 0 "$on" 1 880000001;                                  espera "$m-PE-04" "Cambiar a $otro sin reiniciar: la persona no existe allí" "[ERR-03] No existe una persona con cc 880000001 en $otro"
	entra 0 "$n" 1 880000001;                                   espera "$m-PE-05" "Volver a $db: la persona sigue ahí" "Hu026"
	entra 2;                                                    espera "$m-PE-06" "Listar personas" "880000001"
	entra 7 0 100;                                              espera "$m-PE-07" "Listar parcial de personas" "Página 0 · 6 registros"
	entra 5 880000001 Editada Hu026 F 26;                       espera "$m-PE-08" "Editar persona: muestra el registro actual" "Actual:"
	                                                         espera "$m-PE-09" "Editar persona" "Editada:"
	entra 1 880000001;                                          espera "$m-PE-10" "Consultar la persona editada" "Editada"
	entra 3;                                                    espera "$m-PE-11" "Contar tras crear" "Total en $db: 6"
	entra 8 123456789;                                          espera "$m-33-01" "Pepe con sus teléfonos (HU-033)" "Teléfonos en $db: 2"
	entra 8 147258369;                                          espera "$m-33-02" "Pepita sin teléfonos (HU-033)" "(sin teléfonos)"
	entra 1 999;                                                espera "$m-NE-01" "Persona inexistente" "[ERR-03] No existe una persona con cc 999 en $db"
	entra 4 880000001 Otra Vez F 30;                            espera "$m-NE-02" "Persona duplicada" "[ERR-04]"
	entra 6 123456789;                                          espera "$m-NE-03" "Eliminar persona con dependientes" "[ERR-06]"
	entra abc 3;                                                espera "$m-NE-04" "Opción no numérica: se vuelve a pedir" "[ERR-01] 'abc' no es un número entero"
	entra 0 0

	# Profesión
	entra 2 "$n"
	entra 3;                                                    espera "$m-PR-01" "Contar profesiones" "Total en $db: 2"
	entra 4 880 Verificacion "Creada por HU-026";               espera "$m-PR-02" "Crear profesión" "Creada:"
	entra 1 880;                                                espera "$m-PR-03" "Consultar profesión" "Creada por HU-026"
	entra 2;                                                    espera "$m-PR-04" "Listar profesiones" "880"
	entra 7 0 100;                                              espera "$m-PR-05" "Listar parcial de profesiones" "Página 0 · 3 registros"
	entra 5 880 "Verificacion editada" "Editada por HU-026";    espera "$m-PR-06" "Editar profesión" "Editada:"
	entra 1 880;                                                espera "$m-PR-07" "Consultar la profesión editada" "Editada por HU-026"
	entra 3;                                                    espera "$m-PR-08" "Contar tras crear" "Total en $db: 3"
	entra 4 881 "";                                             espera "$m-NE-05" "Profesión sin nombre" "[ERR-01]"
	entra ""
	entra 6 1;                                                  espera "$m-NE-06" "Eliminar profesión con estudios" "[ERR-06]"
	entra 0 0

	# Teléfono
	entra 3 "$n"
	entra 3;                                                    espera "$m-TE-01" "Contar teléfonos" "Total en $db: 3"
	entra 4 0088000001 Claro 880000001;                         espera "$m-TE-02" "Crear teléfono con cero inicial" "Creado:"
	entra 1 0088000001;                                         espera "$m-TE-03" "Consultar teléfono" "0088000001"
	entra 2;                                                    espera "$m-TE-04" "Listar teléfonos" "3209876543"
	entra 7 0 2;                                                espera "$m-TE-05" "Página 0 de tamaño 2" "Página 0 · 2 registros"
	entra 7 1 2;                                                espera "$m-TE-06" "Página 1 de tamaño 2" "Página 1 · 2 registros"
	entra 5 0088000001 Tigo 123456789;                          espera "$m-TE-07" "Editar teléfono" "Editado:"
	entra 1 0088000001;                                         espera "$m-TE-08" "Consultar el teléfono editado" "Tigo"
	entra 3;                                                    espera "$m-TE-09" "Contar tras crear" "Total en $db: 4"
	entra 4 0088000002 Claro 999;                               espera "$m-NE-07" "Teléfono con dueño inexistente" "[ERR-05]"
	entra 4 0088000001 Claro 123456789;                         espera "$m-NE-08" "Teléfono duplicado" "[ERR-04]"
	entra 0 0

	# Estudios
	entra 4 "$n"
	entra 3;                                                    espera "$m-ES-01" "Contar estudios" "Total en $db: 2"
	entra 4 880 880000001 2020-06-15 Javeriana;                 espera "$m-ES-02" "Crear estudio" "Creado:"
	entra 1 880 880000001;                                      espera "$m-ES-03" "Consultar estudio" "Javeriana"
	entra 2;                                                    espera "$m-ES-04" "Listar estudios" "880"
	entra 7 0 100;                                              espera "$m-ES-05" "Listar parcial de estudios" "Página 0 · 3 registros"
	entra 5 880 880000001 2015-13-45;                           espera "$m-NE-09" "Fecha imposible: se vuelve a pedir" "[ERR-01] La fecha '2015-13-45' no es válida"
	entra 2021-12-01 Nacional;                                  espera "$m-ES-06" "Editar estudio" "Editado:"
	entra 1 880 880000001;                                      espera "$m-ES-07" "Consultar el estudio editado" "Nacional"
	entra 3;                                                    espera "$m-ES-08" "Contar tras crear" "Total en $db: 3"
	entra 4 99 147258369 "" "";                                 espera "$m-NE-10" "Estudio con profesión inexistente" "[ERR-05] No existe una profesión con id 99 en $db"
	entra 4 880 880000001 "" "";                                espera "$m-NE-11" "Estudio duplicado" "[ERR-04]"
	entra 6 880 880000001;                                      espera "$m-ES-09" "Eliminar estudio" "Eliminado el estudio (880, 880000001) en $db"
	entra 1 880 880000001;                                      espera "$m-ES-10" "Consultar el estudio eliminado" "[ERR-03]"
	entra 3;                                                    espera "$m-ES-11" "El conteo vuelve al inicial" "Total en $db: 2"
	entra 0 0

	# Limpieza: eliminar en orden inverso a las dependencias
	entra 3 "$n"
	entra 6 0088000001;                                         espera "$m-TE-10" "Eliminar teléfono" "Eliminado el teléfono 0088000001 en $db"
	entra 1 0088000001;                                         espera "$m-TE-11" "Consultar el teléfono eliminado" "[ERR-03]"
	entra 3;                                                    espera "$m-TE-12" "El conteo vuelve al inicial" "Total en $db: 3"
	entra 0 0
	entra 2 "$n"
	entra 6 880;                                                espera "$m-PR-09" "Eliminar profesión" "Eliminada la profesión con id 880 en $db"
	entra 1 880;                                                espera "$m-PR-10" "Consultar la profesión eliminada" "[ERR-03]"
	entra 3;                                                    espera "$m-PR-11" "El conteo vuelve al inicial" "Total en $db: 2"
	entra 0 0
	entra 1 "$n"
	entra 6 880000001;                                          espera "$m-PE-12" "Eliminar persona" "Eliminada la persona con cc 880000001 en $db"
	entra 1 880000001;                                          espera "$m-PE-13" "Consultar la persona eliminada" "[ERR-03]"
	entra 3;                                                    espera "$m-PE-14" "El conteo vuelve al inicial" "Total en $db: 5"
	entra 0 0
}

entra 1 7;  espera "GE-01" "Motor no válido en el menú" "[ERR-02] Opción de motor no válida"
entra 0
bloque 1 MARIA 2 MONGO
bloque 2 MONGO 1 MARIA
entra 0

printf '%s' "$ENTRADA" | $CLI > "$SALIDA" 2>&1
CODIGO=$?
SALIDA_TXT="$(cat "$SALIDA")"

echo "## Verificación CLI (HU-026)"
echo
echo "- Fecha: $(date '+%Y-%m-%d %H:%M %z')"
echo "- Commit: $(git rev-parse --short HEAD 2>/dev/null || echo 'sin repositorio')"
echo "- Comando: \`$CLI\`, una sola sesión; código de salida $CODIGO"
echo
echo '| ID | Caso | Texto esperado en la salida, en orden | Estado |'
echo '|---|---|---|---|'
TOTAL=0
FALLAS=0
resto="$SALIDA_TXT"
for e in "${ESPERADOS[@]}"; do
	IFS='|' read -r id caso texto <<<"$e"
	TOTAL=$((TOTAL + 1))
	if [[ "$resto" == *"$texto"* ]]; then
		resto="${resto#*"$texto"}"
		estado="OK"
	else
		estado="**FALLA**"
		FALLAS=$((FALLAS + 1))
	fi
	printf '| %s | %s | `%s` | %s |\n' "$id" "$caso" "$texto" "$estado"
done
echo
echo "**Resultado:** $((TOTAL - FALLAS)) de $TOTAL comprobaciones correctas, $FALLAS fallas. Salida completa en \`$SALIDA\`."
[ "$FALLAS" -eq 0 ] && [ "$CODIGO" -eq 0 ]
