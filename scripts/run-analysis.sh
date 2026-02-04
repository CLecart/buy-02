#!/usr/bin/env bash
set -euo pipefail

# Run the project's static analysis (SpotBugs, PMD, Checkstyle) using JDK 17.
# Preferred: run inside a JDK17 Docker image to ensure consistent tooling.

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "Running analysis for project at $ROOT_DIR"

if command -v docker >/dev/null 2>&1; then
  echo "Docker detected — running analysis inside Temurin 17 container"
  docker run --rm -v "$ROOT_DIR":/work -w /work eclipse-temurin:17-jdk bash -lc \
    "mvn -B -V -T1C -Panalysis -DskipTests -DskipITs spotbugs:spotbugs pmd:pmd checkstyle:check"
  exit $?
fi

if command -v mvn >/dev/null 2>&1; then
  JAVA_HOME=${JAVA_HOME:-}
  if java -version 2>&1 | grep -q "17"; then
    echo "Local Java 17 detected — running mvn -Panalysis locally"
    mvn -B -V -T1C -Panalysis -DskipTests -DskipITs spotbugs:spotbugs pmd:pmd checkstyle:check
    exit $?
  else
    echo "No Docker and local Java is not 17. To run analysis either:"
    echo "  - install Docker and re-run this script, or"
    echo "  - install a JDK 17 and set it as active, then re-run this script."
    exit 2
  fi
fi

echo "Neither Docker nor mvn command found. Install Maven or Docker to run analysis."
exit 3
