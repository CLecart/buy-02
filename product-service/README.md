# Product Service

Product management microservice for the buy-02 e-commerce platform.

## Features

- Full CRUD operations for products
- Role-based access (SELLER only for create/update/delete)
- Ownership enforcement (sellers can only modify their own products)
- Pagination support for product listing
- Kafka event publishing (ProductCreatedEvent, ProductUpdatedEvent, ProductDeletedEvent)
- Kafka event consumption (UserDeletedEvent for cascade deletion)

## API Endpoints

| Method | Endpoint                          | Description                   | Auth         |
| ------ | --------------------------------- | ----------------------------- | ------------ |
| GET    | `/api/products`                   | List all products (paginated) | -            |
| GET    | `/api/products/{id}`              | Get product by ID             | -            |
| GET    | `/api/products/seller/{sellerId}` | Get products by seller        | -            |
| POST   | `/api/products`                   | Create product                | JWT + SELLER |
| PUT    | `/api/products/{id}`              | Update product                | JWT + OWNER  |
| DELETE | `/api/products/{id}`              | Delete product                | JWT + OWNER  |

## Configuration

| Variable                  | Description        | Default         |
| ------------------------- | ------------------ | --------------- |
| `APP_JWT_SECRET`          | JWT signing key    | Required        |
| `SPRING_DATA_MONGODB_URI` | MongoDB connection | localhost:27018 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker       | localhost:9092  |
| `SSL_ENABLED`             | Enable HTTPS       | false           |

## Build & Run

```bash
# Local development
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"

# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/product-service-*.jar --server.port=8082

# Docker
docker build -t buy02-product-service .
docker run -p 8082:8080 -e APP_JWT_SECRET=... buy02-product-service
```

## Testing

```bash
mvn test
```

## Kafka Events

**Published:**

- `product-events` topic: `ProductCreatedEvent`, `ProductUpdatedEvent`, `ProductDeletedEvent`

**Consumed:**

- `user-events` topic: `UserDeletedEvent` → deletes all products owned by the user

## Product Model

```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "price": 0.0,
  "quantity": 0,
  "ownerId": "string",
  "mediaIds": ["string"]
}
```
