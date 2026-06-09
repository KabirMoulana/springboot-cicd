#!/bin/sh
set -e
# Find java
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
fi
# Download maven if needed
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.6"
if [ ! -d "$MAVEN_HOME" ]; then
  mkdir -p "$MAVEN_HOME"
  curl -sL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.tar.gz" | tar xz -C "$MAVEN_HOME" --strip-components=1
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
