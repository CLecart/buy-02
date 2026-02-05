# buy-02 — E-Commerce Microservices Platform

An end-to-end e-commerce platform built with **Spring Boot 3.x microservices** and **Angular 17+**. The platform supports user registration (client/seller), JWT authentication, product CRUD for sellers, media management with image uploads, and inter-service communication via **Apache Kafka**.

## Features

- **User Management**: Registration as CLIENT or SELLER, JWT authentication, avatar upload for sellers
- **Product Management**: Full CRUD operations (SELLER only), ownership enforcement, search/filtering
- **Media Management**: Image uploads with 2MB limit, type validation (JPEG, PNG, GIF)
- **Orders & Cart**: Shopping cart, order creation, cancellation, redo, and status tracking
- **Profiles**: Buyer and seller analytics (top products, spending/revenue)
- **Event-Driven Architecture**: Kafka for inter-service communication (cascade deletions)
- **Security**: BCrypt password hashing, role-based access control, HTTPS support
- **Frontend**: Angular SPA with authentication, product listing, seller dashboard

## Project Structure

```
buy-02/
├── pom.xml                     # Parent POM (Java 21, Spring Boot 3.2)
├── docker-compose.dev.yml      # Full stack: MongoDB, Kafka, services, frontend
├── .env.example                # Environment template
├── config/ssl/                 # SSL certificates (not committed)
├── shared-lib/                 # Common: JWT, DTOs, Kafka events, security filters
├── user-service/               # Auth & user management (port 8081)
├── product-service/            # Product CRUD (port 8082)
├── media-service/              # Media uploads (port 8083)
├── order-service/              # Orders & cart (port 8084)
└── frontend-angular/           # Angular 17+ SPA (port 4200)
```

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose v2+
- Node.js 18+ (for frontend development)

### Run with Docker (Recommended)

```bash
# Clone and setup
git clone <repo-url> && cd buy-02
cp .env.example .env

# Generate a secure JWT secret
echo "APP_JWT_SECRET=$(openssl rand -base64 32)" >> .env

# Start all services
docker compose -f docker-compose.dev.yml up -d --build

# Access points:
# - Frontend:        http://localhost:4200
# - User Service:    http://localhost:8081
# - Product Service: http://localhost:8082
# - Media Service:   http://localhost:8083
# - Order Service:   http://localhost:8084
```

### Run Locally (Development)

```bash
# Start infrastructure
docker compose -f docker-compose.dev.yml up -d mongo kafka zookeeper

# Set environment
export APP_JWT_SECRET=$(openssl rand -base64 32)
export MONGO_URI='mongodb://root:example@localhost:27019/buy02?authSource=admin'

# Run services (separate terminals)
mvn -pl user-service spring-boot:run
mvn -pl product-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
mvn -pl media-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
mvn -pl order-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8084"

# Run frontend
cd frontend-angular && npm install && npm start
```

## HTTPS/SSL Configuration

SSL is disabled by default for development. To enable HTTPS:

```bash
# Generate self-signed certificate (development only)
mkdir -p config/ssl && cd config/ssl
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes \
  -subj "/C=FR/ST=IDF/L=Paris/O=Buy02/CN=localhost"
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12 -name buy02 -passout pass:changeit

# Copy to services
cp keystore.p12 ../../user-service/src/main/resources/
cp keystore.p12 ../../product-service/src/main/resources/
cp keystore.p12 ../../media-service/src/main/resources/

# Enable in .env
SSL_ENABLED=true
```

For production, use **Let's Encrypt** or a proper CA certificate.

## Testing

```bash
# Run all tests
mvn clean test

# Run specific service tests
mvn -pl product-service test

# Run with coverage
mvn clean verify
```

## API Endpoints

### User Service (8081)

| Method | Endpoint               | Description          | Auth         |
| ------ | ---------------------- | -------------------- | ------------ |
| POST   | `/api/auth/signup`     | Register user        | -            |
| POST   | `/api/auth/signin`     | Login                | -            |
| GET    | `/api/users/me`        | Current user profile | JWT          |
| POST   | `/api/users/me/avatar` | Upload avatar        | JWT + SELLER |

### Product Service (8082)

| Method | Endpoint             | Description       | Auth         |
| ------ | -------------------- | ----------------- | ------------ |
| GET    | `/api/products`      | List all products | -            |
| GET    | `/api/products/{id}` | Get product       | -            |
| POST   | `/api/products`      | Create product    | JWT + SELLER |
| PUT    | `/api/products/{id}` | Update product    | JWT + OWNER  |
| DELETE | `/api/products/{id}` | Delete product    | JWT + OWNER  |

### Media Service (8083)

| Method | Endpoint                  | Description        | Auth         |
| ------ | ------------------------- | ------------------ | ------------ |
| POST   | `/api/media/upload`       | Upload image       | JWT + SELLER |
| GET    | `/api/media/{id}`         | Get media file     | -            |
| GET    | `/api/media/product/{id}` | List product media | -            |
| DELETE | `/api/media/{id}`         | Delete media       | JWT + OWNER  |

### Order Service (8084)

| Method | Endpoint                   | Description          | Auth         |
| ------ | -------------------------- | -------------------- | ------------ |
| GET    | `/api/orders/me`           | List my orders       | JWT          |
| GET    | `/api/orders/seller/me`    | List seller orders   | JWT + SELLER |
| POST   | `/api/orders`              | Create order         | JWT          |
| PATCH  | `/api/orders/{id}/cancel`  | Cancel order         | JWT          |
| POST   | `/api/orders/{id}/redo`    | Redo order           | JWT          |
| DELETE | `/api/orders/{id}`         | Remove order         | JWT          |
| GET    | `/api/carts/me`            | Get my cart          | JWT          |
| POST   | `/api/carts/me/items`      | Add to cart          | JWT          |
| PATCH  | `/api/carts/me/items/{id}` | Update cart quantity | JWT          |
| DELETE | `/api/carts/me/items/{id}` | Remove cart item     | JWT          |

## Architecture

```
                                    ┌──────────────────┐
                                    │  Angular Frontend │
                                    │      :4200       │
                                    └────────┬─────────┘
                                             │ HTTP/HTTPS
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
          ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
          │  user-service   │     │ product-service │     │  media-service  │
          │     :8081       │     │     :8082       │     │     :8083       │
          │                 │     │                 │     │                 │
          │ • Auth (JWT)    │     │ • Product CRUD  │     │ • Image Upload  │
          │ • User CRUD     │     │ • Ownership     │     │ • 2MB Limit     │
          │ • Avatar Upload │     │                 │     │ • Type Valid.   │
          └────────┬────────┘     └────────┬────────┘     └────────┬────────┘
                   │                       │                       │
                   │    ┌──────────────────┴───────────────────┐   │
                   │    │                                      │   │
                   ▼    ▼                                      ▼   ▼
          ┌─────────────────┐                         ┌─────────────────┐
          │    MongoDB      │                         │     Kafka       │
          │     :27017      │                         │     :9092       │
          │                 │                         │                 │
          │ • userdb        │                         │ • user-events   │
          │ • productdb     │                         │ • product-events│
          │ • mediadb       │                         │                 │
          └─────────────────┘                         └─────────────────┘
```

### Event-Driven Communication (Kafka)

```
┌─────────────┐  UserDeletedEvent   ┌─────────────────┐  ProductDeletedEvent  ┌─────────────┐
│user-service │ ─────────────────▶  │ product-service │ ─────────────────────▶│media-service│
└─────────────┘                     └─────────────────┘                       └─────────────┘
       │                                                                             ▲
       │                          UserDeletedEvent                                   │
       └─────────────────────────────────────────────────────────────────────────────┘
```

**Cascade Deletion Flow:**

1. User deleted → `UserDeletedEvent` published
2. product-service receives → deletes all user's products → publishes `ProductDeletedEvent`
3. media-service receives both events → deletes all related media files

### Kafka Topics

| Topic            | Producer        | Consumer                       | Event               |
| ---------------- | --------------- | ------------------------------ | ------------------- |
| `user-events`    | user-service    | product-service, media-service | UserDeletedEvent    |
| `product-events` | product-service | media-service                  | ProductDeletedEvent |

## Security

- **Password Hashing**: BCrypt with configurable strength
- **JWT Authentication**: Stateless, configurable expiration
- **Role-Based Access**: SELLER/CLIENT roles enforced at endpoint level
- **Ownership Validation**: Users can only modify their own resources
- **Input Validation**: Jakarta Bean Validation on all DTOs
- **HTTPS**: TLS encryption support (configurable)

## Environment Variables

| Variable                  | Description                    | Default         |
| ------------------------- | ------------------------------ | --------------- |
| `APP_JWT_SECRET`          | JWT signing key (min 32 chars) | Required        |
| `MONGO_URI`               | MongoDB connection string      | localhost:27018 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address           | localhost:9092  |
| `STORAGE_PATH`            | Media storage directory        | ./data/storage  |
| `SSL_ENABLED`             | Enable HTTPS                   | false           |
| `SSL_KEYSTORE_PASSWORD`   | Keystore password              | changeit        |

## License

This project is for educational purposes (Zone01 curriculum).
