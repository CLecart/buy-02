# Quick start (audit)

Cette page décrit les étapes minimales pour reproduire l'environnement utilisé lors de l'audit et collecter les artefacts (logs et réponses HTTP).

## Option A — Docker Compose (recommandée)

Prérequis : Docker et Docker Compose v2+.

> **Note** : Le container MongoDB expose le port `27019` sur l'hôte (pour éviter les conflits avec une instance MongoDB locale sur 27017). Les services Docker communiquent en interne via `mongo:27017`.

```bash
# Créer un .env local (NE PAS committer)
cp .env.example .env
# Générer un secret JWT temporaire
echo "APP_JWT_SECRET=$(openssl rand -base64 32)" >> .env
chmod 600 .env

# Démarrer toute la stack (mongo, services, frontend)
docker compose -f docker-compose.dev.yml up -d --build

# Frontend disponible sur http://localhost:4200
# API user-service sur http://localhost:8081
# API product-service sur http://localhost:8082
# API media-service sur http://localhost:8083
# API order-service sur http://localhost:8084
```

Pour arrêter :

```bash
docker compose -f docker-compose.dev.yml down
```

---

## Option B — Services locaux (Maven)

Prérequis:

- Java 21, Maven, `curl`, `jq` (optionnel) et une instance MongoDB accessible (ex: `mongodb://localhost:27017/buy02`).

1. Générer un secret temporaire (NE PAS committer) :

```bash
tr -dc 'A-Za-z0-9' </dev/urandom | head -c32 > /tmp/audit_jwt_secret
chmod 600 /tmp/audit_jwt_secret
export APP_JWT_SECRET=$(cat /tmp/audit_jwt_secret)
export MONGO_URI='mongodb://localhost:27017/buy02'
```

2. Lancer les services (4 terminaux séparés ou en background) :

```bash
# user-service (port 8081)
APP_JWT_SECRET="$APP_JWT_SECRET" MONGO_URI="$MONGO_URI" mvn -pl user-service spring-boot:run

# product-service (port 8082)
APP_JWT_SECRET="$APP_JWT_SECRET" MONGO_URI="$MONGO_URI" mvn -pl product-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"

# media-service (port 8083)
APP_JWT_SECRET="$APP_JWT_SECRET" MONGO_URI="$MONGO_URI" mvn -pl media-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"

# order-service (port 8084)
APP_JWT_SECRET="$APP_JWT_SECRET" MONGO_URI="$MONGO_URI" mvn -pl order-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8084"
```

3. Exécuter le smoke-test (exemple inline ou script) :

```bash
# exemple inline
ts=$(date -u +"%Y%m%dT%H%M%SZ")
email="audit+$ts@example.com"
mkdir -p out/smoke out/logs

# signup
curl -s -X POST http://localhost:8081/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"Audit User\",\"email\":\"$email\",\"password\":\"Passw0rd1\",\"role\":\"SELLER\"}" \
  -o out/smoke/signup_resp.json

# extract token
token=$(python3 -c "import json; print(json.load(open('out/smoke/signup_resp.json'))['token'])")

# create product
curl -s -X POST http://localhost:8082/api/products \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $token" \
  -d '{"name":"Test Product","description":"smoke","price":9.99}' \
  -o out/smoke/product_resp.json

product_id=$(python3 -c "import json; print(json.load(open('out/smoke/product_resp.json'))['id'])")

# upload 1x1 PNG
echo 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=' | base64 -d > out/smoke/test.png
curl -s -X POST http://localhost:8083/api/media/upload \
  -H "Authorization: Bearer $token" \
  -F "file=@out/smoke/test.png" \
  -F "productId=$product_id" \
  -o out/smoke/upload_resp.json

echo "Done. Check out/smoke/ for results."
```

4. Archiver les artefacts d'audit :

```bash
tar -C out -czf out/audit-smoke-$(date -u +%Y%m%dT%H%M%SZ).tar.gz smoke logs
sha256sum out/audit-smoke-*.tar.gz > out/audit-smoke.sha256
```

5. Arrêter les services et nettoyer le secret :

```bash
pkill -f 'user-service' || true
pkill -f 'product-service' || true
pkill -f 'media-service' || true
pkill -f 'order-service' || true
shred -u /tmp/audit_jwt_secret || rm -f /tmp/audit_jwt_secret
```

---

## Tests d'integration (Testcontainers)

Prerequis : Docker en fonctionnement.

```bash
mvn verify -Pintegration
```

---

Veillez à ne jamais committer le fichier `/tmp/audit_jwt_secret`, `.env` ni tout secret utilisé localement.
