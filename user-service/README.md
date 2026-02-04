# User Service

Authentication and user management microservice for the buy-02 e-commerce platform.

## Features

- User registration (CLIENT/SELLER roles)
- JWT authentication (login/logout)
- Profile management
- Avatar upload for sellers (2MB limit, JPEG/PNG/GIF)
- Kafka event publishing (UserDeletedEvent)
- BCrypt password hashing

## API Endpoints

| Method | Endpoint               | Description              | Auth         |
| ------ | ---------------------- | ------------------------ | ------------ |
| POST   | `/api/auth/signup`     | Register new user        | -            |
| POST   | `/api/auth/signin`     | Login, returns JWT       | -            |
| GET    | `/api/users/me`        | Get current user profile | JWT          |
| PUT    | `/api/users/me`        | Update profile           | JWT          |
| DELETE | `/api/users/me`        | Delete account           | JWT          |
| POST   | `/api/users/me/avatar` | Upload avatar            | JWT + SELLER |
| GET    | `/api/users/me/avatar` | Get avatar               | JWT          |

## Configuration

| Variable                  | Description         | Default         |
| ------------------------- | ------------------- | --------------- |
| `APP_JWT_SECRET`          | JWT signing key     | Required        |
| `SPRING_DATA_MONGODB_URI` | MongoDB connection  | localhost:27018 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker        | localhost:9092  |
| `STORAGE_PATH`            | Avatar storage path | ./data/storage  |
| `SSL_ENABLED`             | Enable HTTPS        | false           |

## Build & Run

```bash
# Local development
mvn spring-boot:run

# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/user-service-*.jar

# Docker
docker build -t buy02-user-service .
docker run -p 8081:8080 -e APP_JWT_SECRET=... buy02-user-service
```

## Testing

```bash
mvn test
```

## Kafka Events

**Published:**

- `user-events` topic: `UserDeletedEvent` when a user is deleted

This triggers cascade deletion of user's products and media in other services.
