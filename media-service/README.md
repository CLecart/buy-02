# Media Service

Media management microservice for the buy-02 e-commerce platform.

## Features

- Image upload with validation (2MB max, JPEG/PNG/GIF only)
- Apache Tika for MIME type detection (prevents extension spoofing)
- Local file storage with configurable path
- Role-based access (SELLER only for upload/delete)
- Ownership enforcement
- Kafka event consumption (ProductDeletedEvent, UserDeletedEvent for cascade deletion)

## API Endpoints

| Method | Endpoint                         | Description            | Auth         |
| ------ | -------------------------------- | ---------------------- | ------------ |
| POST   | `/api/media/upload`              | Upload image           | JWT + SELLER |
| GET    | `/api/media/{id}`                | Get media file         | -            |
| GET    | `/api/media/{id}/metadata`       | Get media metadata     | -            |
| GET    | `/api/media/product/{productId}` | List media for product | -            |
| GET    | `/api/media/owner/{ownerId}`     | List media by owner    | JWT          |
| DELETE | `/api/media/{id}`                | Delete media           | JWT + OWNER  |

## Upload Constraints

- **Maximum file size**: 2 MB
- **Allowed types**: `image/jpeg`, `image/png`, `image/gif`
- **Validation**: Apache Tika detects actual MIME type (not just extension)

## Configuration

| Variable                   | Description             | Default               |
| -------------------------- | ----------------------- | --------------------- |
| `APP_JWT_SECRET`           | JWT signing key         | Required              |
| `SPRING_DATA_MONGODB_URI`  | MongoDB connection      | localhost:27018       |
| `KAFKA_BOOTSTRAP_SERVERS`  | Kafka broker            | localhost:9092        |
| `STORAGE_PATH`             | Media storage directory | ./data/storage        |
| `PRODUCT_SERVICE_BASE_URL` | Product service URL     | http://localhost:8082 |
| `SSL_ENABLED`              | Enable HTTPS            | false                 |

## Build & Run

```bash
# Local development
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"

# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/media-service-*.jar --server.port=8083

# Docker
docker build -t buy02-media-service .
docker run -p 8083:8080 -e APP_JWT_SECRET=... buy02-media-service
```

## Testing

```bash
mvn test
```

## Kafka Events

**Consumed:**

- `product-events` topic: `ProductDeletedEvent` → deletes all media for the product
- `user-events` topic: `UserDeletedEvent` → deletes all media owned by the user

## Example Requests

**Upload image:**

```bash
curl -X POST http://localhost:8083/api/media/upload \
  -H "Authorization: Bearer <JWT>" \
  -F "file=@image.jpg" \
  -F "productId=prod-123"
```

**Get media:**

```bash
curl http://localhost:8083/api/media/{id}
```

**Delete media:**

```bash
curl -X DELETE http://localhost:8083/api/media/{id} \
  -H "Authorization: Bearer <JWT>"
```

- Replace `http://localhost:8081` with your local server URL and port.
- Use a valid JWT for protected endpoints (see service configuration).

Security & ownership

- Protected endpoints: `POST /api/media`, `GET /api/media/{id}` and `DELETE /api/media/{id}` require a valid JWT presented in the `Authorization: Bearer <JWT>` header.
- Ownership rule: `DELETE /api/media/{id}` can only be performed by the `ownerId` recorded in the media metadata. The server enforces this and returns `403 Forbidden` when the authenticated principal is not the owner.
