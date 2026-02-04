# Frontend Angular

Angular 17+ SPA for the buy-02 e-commerce platform.

## Features

- User authentication (sign in/sign up)
- Role-based UI (CLIENT vs SELLER views)
- Product listing with images
- Seller dashboard for product management
- Media upload with drag & drop
- JWT token management with HTTP interceptor
- Responsive design with SCSS
- Dark/Light theme toggle

## Pages

| Route        | Component                | Description               |
| ------------ | ------------------------ | ------------------------- |
| `/`          | ProductListComponent     | Public product listing    |
| `/signin`    | SigninComponent          | User login                |
| `/signup`    | SignupComponent          | User registration         |
| `/dashboard` | SellerDashboardComponent | Seller product management |

## Requirements

- Node.js >= 18
- npm >= 9

## Installation

```bash
cd frontend-angular
npm ci          # Install exact versions from lockfile
```

## Development

```bash
npm start       # Starts on http://localhost:4200
```

The dev server uses `proxy.conf.json` to route API calls:

- `/api/auth/*`, `/api/users/*` → localhost:8081 (user-service)
- `/api/products/*` → localhost:8082 (product-service)
- `/api/media/*` → localhost:8083 (media-service)

## Build

```bash
npm run build   # Production build in dist/
```

## Docker

```bash
docker build -t buy02-frontend .
docker run -p 4200:4200 buy02-frontend
```

Or run with full stack:

```bash
docker compose -f docker-compose.dev.yml up -d
```

## Project Structure

```
src/app/
├── components/          # Reusable components
│   └── theme-toggle/    # Dark/light theme switcher
├── interceptors/        # HTTP interceptors (JWT)
├── models/              # TypeScript interfaces
├── pages/               # Page components
│   ├── product-list/    # Public product listing
│   ├── seller-dashboard/# Seller product management
│   ├── signin/          # Login page
│   └── signup/          # Registration page
├── services/            # Angular services
│   ├── auth.service.ts  # Authentication
│   ├── product.service.ts # Product API
│   └── media.service.ts # Media API
├── app.config.ts        # App configuration
└── app.routes.ts        # Route definitions
```

## Environment

API base URLs are configured via `proxy.conf.json` for development.
For production, update `environment.prod.ts` with actual service URLs.

## Notes for Auditors

- `package-lock.json` committed for reproducible builds
- Use `npm ci` in CI pipelines
- Standalone components (Angular 17+ style)
