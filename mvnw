#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.
set -e

# Determine the script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_WRAPPER_PROPERTIES="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties"

# Read distribution URL
DIST_URL="$(grep distributionUrl "$MAVEN_WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')"
MVN_VERSION="3.9.6"
MVN_HOME="$HOME/.m2/wrapper/dists/apache-maven-${MVN_VERSION}"

if [ ! -f "$MVN_HOME/bin/mvn" ]; then
  echo "Downloading Apache Maven ${MVN_VERSION}..."
  mkdir -p "$MVN_HOME"
  TMP_FILE=$(mktemp)
  curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MVN_VERSION}/apache-maven-${MVN_VERSION}-bin.tar.gz" \
    -o "$TMP_FILE"
  tar xzf "$TMP_FILE" -C "$MVN_HOME" --strip-components=1
  rm -f "$TMP_FILE"
fi

exec "$MVN_HOME/bin/mvn" "$@"
