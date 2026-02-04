#!/usr/bin/env bash
set -euo pipefail

# Lightweight smoke script to build images, start docker-compose and run simple endpoint checks.
# Requires docker and docker-compose available on the host.

COMPOSE_FILE="docker-compose.dev.yml"
OUT_LOG=${1:-/tmp/ci-smoke.log}

echo "Starting CI smoke: logs -> $OUT_LOG"
exec >"$OUT_LOG" 2>&1

echo "Building images for services with Dockerfile..."
for svc in user-service product-service media-service frontend; do
  if [ -f "$svc/Dockerfile" ]; then
    echo "Building image for $svc"
    docker build -t buy-02-$svc:ci "$svc"
  else
    echo "No Dockerfile for $svc, skipping build"
  fi
done

echo "Bringing up compose stack (detached)"
docker compose -f "$COMPOSE_FILE" up -d --remove-orphans

echo "Waiting for services to become healthy (simple sleep)"
sleep 8

# Minimal checks: ensure key ports return HTTP status 200 or at least a TCP listener
echo "Checking service endpoints"
set +e
curl -sS --max-time 5 http://localhost:8081/actuator/health >/dev/null 2>&1; U1=$?
curl -sS --max-time 5 http://localhost:8082/actuator/health >/dev/null 2>&1; U2=$?
curl -sS --max-time 5 http://localhost:8083/actuator/health >/dev/null 2>&1; U3=$?
curl -sS --max-time 5 http://localhost:4200/ >/dev/null 2>&1; U4=$?
set -e

echo "Results: user($U1) product($U2) media($U3) frontend($U4)"

FAIL=0
if [ $U1 -ne 0 ]; then echo "user-service health check failed"; FAIL=1; fi
if [ $U2 -ne 0 ]; then echo "product-service health check failed"; FAIL=1; fi
if [ $U3 -ne 0 ]; then echo "media-service health check failed"; FAIL=1; fi
if [ $U4 -ne 0 ]; then echo "frontend check failed"; FAIL=1; fi

echo "Tearing down compose stack"
docker compose -f "$COMPOSE_FILE" down

if [ $FAIL -ne 0 ]; then
  echo "Smoke checks failed, see log: $OUT_LOG"
  exit 2
fi

echo "Smoke checks passed"
exit 0
