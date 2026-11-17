#!/bin/sh
set -e

# Compilation continue (polling) pour Docker Desktop / Windows :
# DevTools redémarre l'app dès que target/classes change.
(
	LAST=""
	while true; do
		CUR=$(find src -type f \( -name '*.java' -o -name '*.properties' -o -name '*.yml' -o -name '*.yaml' -o -name '*.xml' \) -print0 2>/dev/null | xargs -0 ls -l 2>/dev/null | cksum | awk '{print $1}')
		if [ "$CUR" != "$LAST" ]; then
			echo "[watch] changement détecté → compilation…"
			if ./mvnw -o -q compile -DskipTests; then
				echo "[watch] compilation OK"
			else
				echo "[watch] compilation en échec"
			fi
			LAST="$CUR"
		fi
		sleep 2
	done
) &

exec ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.poll-interval=2s -Dspring.devtools.restart.quiet-period=1s"
