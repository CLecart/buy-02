# Database migrations and usage

This folder contains SQL migration scripts to create the core ecommerce schema used
by the services in this project (carts, orders, wishlists, and simple analytics profiles).

Files

- `migrations/V1__create_ecommerce_schema.sql` : initial schema for carts, cart_items,
  orders, order_items, wishlists, `user_profiles` and `seller_profiles`.

Applying the migration

1. PostgreSQL (manual):

   ```bash
   psql -h <host> -U <user> -d <database> -f database/migrations/V1__create_ecommerce_schema.sql
   ```

2. Using Flyway (recommended for production):
   - Copy `V1__create_ecommerce_schema.sql` into your Flyway `sql` directory (e.g. `db/migration`).
   - Configure Flyway to point to the target DB and run `flyway migrate`.

Notes and recommendations

- The SQL is written for PostgreSQL (uses `JSONB`). For MySQL or other DBs adapt types.
- IDs are `VARCHAR(36)` to support UUID strings; you can replace with native UUID types if desired.
- Consider using a proper migration tool (Flyway or Liquibase) for CI/CD integration.
