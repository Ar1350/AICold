#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
echo "Compiling..."
# shellcheck disable=SC2046
javac -d out $(find src/main/java -name '*.java')
echo "Starting Elimination Arena on http://localhost:8080 ..."
exec java -cp out com.aicold.game.Main "$@"
