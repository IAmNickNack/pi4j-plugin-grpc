#!/usr/bin/env bash

SERVER_PORT=${1:-9090}
JAR_FILE="$(find . -name pi4j-plugin-grpc-server-all.jar)"

 java \
  --sun-misc-unsafe-memory-access=allow \
  --enable-native-access=ALL-UNNAMED \
  -Dpi4j.server.port="${SERVER_PORT}" \
  ${JAVA_OPTS} -jar "${JAR_FILE}"
