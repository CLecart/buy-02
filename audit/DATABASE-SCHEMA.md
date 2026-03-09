# Database Schema

- MongoDB collections and indexes are implemented and validated by integration tests.
- Source of truth: `docs/DATABASE-SCHEMA.md`.
- Key enforced indexes include:
  - `orders`: `{buyerId, createdAt}`, `{items.sellerId, createdAt}`, `{status, createdAt}`
  - `shopping_carts`: unique `{userId}` and TTL 90 days on `{updatedAt}`
  - `wishlists`: unique `{userId}`
  - `user_profiles`: unique `{userId}`, stats indexes on `{totalOrders}` and `{totalSpent}`
  - `seller_profiles`: unique `{sellerId}`, stats indexes on `{totalRevenue}` and `{averageRating}`

See README.md for schema evidence.
