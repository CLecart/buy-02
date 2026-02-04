# Quick start (audit) — media-service

Guide spécifique pour `media-service` (exécuter localement et tester l'upload).

Variables recommandées :

- `APP_JWT_SECRET` (>= 16 chars) — obligatoire
- `MONGO_URI` — ex: `mongodb://localhost:27017/buy02`

Démarrer le service (port 8083) :

```bash
APP_JWT_SECRET='VOTRE_SECRET_LONG' MONGO_URI='mongodb://localhost:27017/buy02' mvn -pl media-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
```

Exemple d'upload (multipart/form-data) :

```bash
curl -v -X POST "http://localhost:8083/api/media/upload" \
  -H "Authorization: Bearer <TOKEN>" \
  -F "file=@./path/to/image.png" \
  -F "productId=<PRODUCT_ID>"
```

Conseils pour l'audit :

- Utilisez des images valides (`.png`, `.jpg`) — le service valide l'extension/mime.
- Sauvegardez les réponses HTTP dans `out/smoke/` et les logs dans `out/logs/`.
