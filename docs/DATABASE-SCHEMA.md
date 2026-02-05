# Database Schema - buy-02 E-Commerce Platform

## Overview

buy-02 uses **MongoDB** as the primary database. This document describes all collections, fields, indexes, and relationships.

## Collections

### 1. users (Existing)

User account information (from buy-01).

```
{
  _id: ObjectId,
  email: string,
  password: string (hashed),
  role: enum [CLIENT, SELLER],
  avatar: string (media ID),
  createdAt: datetime,
  updatedAt: datetime
}
```

**Indexes:**

- `{email: 1}` - Unique email for login
- `{role: 1}` - Query users by role

---

### 2. products (Existing)

Product catalog (from buy-01).

```
{
  _id: ObjectId,
  name: string,
  description: string,
  sellerId: ObjectId,
  price: decimal,
  stock: number,
  createdAt: datetime,
  updatedAt: datetime
}
```

**Indexes:**

- `{sellerId: 1}` - List products by seller
- `{name: 1}` - Product search
- `{createdAt: -1}` - Recent products

---

### 3. media (Existing)

Product and user images (from buy-01).

```
{
  _id: ObjectId,
  filename: string,
  mimeType: string,
  size: number,
  ownerId: ObjectId,
  productId: ObjectId,
  uploadedAt: datetime
}
```

**Indexes:**

- `{ownerId: 1}` - Media by owner
- `{productId: 1}` - Media for product

---

### 4. orders ⭐ NEW

Purchase orders placed by customers.

```
{
  _id: ObjectId,
  buyerId: ObjectId,
  items: [
    {
      productId: ObjectId,
      sellerId: ObjectId,
      productName: string,
      quantity: number,
      price: decimal,
      subtotal: decimal,
      mediaId: ObjectId
    }
  ],
  totalPrice: decimal,
  shippingCost: decimal,
  status: enum [
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED,
    REFUNDED
  ],
  paymentMethod: enum [
    PAY_ON_DELIVERY,
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    BANK_TRANSFER,
    WALLET
  ],
  paymentReference: string,
  shippingAddress: string,
  trackingNumber: string,
  estimatedDeliveryDate: datetime,
  deliveredAt: datetime,
  notes: string,
  createdAt: datetime,
  updatedAt: datetime
}
```

**Indexes:**

- `{buyerId: 1, createdAt: -1}` - Orders by buyer (compound)
- `{items.sellerId: 1, createdAt: -1}` - Orders by seller (compound)
- `{status: 1, createdAt: -1}` - Orders by status (compound)

**Purpose:**

- Track all customer purchases
- Enable order history and status tracking
- Support order management (cancel, return, refund)

---

### 5. shopping_carts ⭐ NEW

Current shopping carts for active users.

```
{
  _id: ObjectId,
  userId: ObjectId (unique),
  items: [
    {
      productId: ObjectId,
      sellerId: ObjectId,
      productName: string,
      quantity: number,
      price: decimal,
      subtotal: decimal,
      mediaId: ObjectId,
      createdAt: timestamp
    }
  ],
  totalPrice: decimal,
  createdAt: datetime,
  updatedAt: datetime,
  lastModifiedAt: datetime
}
```

**Indexes:**

- `{userId: 1}` - Unique cart per user
- TTL: 90 days (auto-delete inactive carts)

**Purpose:**

- Store persistent shopping carts
- Survive page refreshes and browser sessions
- Calculate total price in real-time

---

### 6. user_profiles ⭐ NEW

Extended customer profile information.

```
{
  _id: ObjectId,
  userId: ObjectId (unique),
  firstName: string,
  lastName: string,
  phone: string,
  address: string,
  city: string,
  postalCode: string,
  country: string,
  totalOrders: number,
  totalSpent: decimal,
  averageOrderValue: decimal,
  favoriteProductIds: [ObjectId],
  mostPurchasedProductIds: [ObjectId],
  avatarMediaId: ObjectId,
  lastOrderDate: datetime,
  createdAt: datetime,
  updatedAt: datetime
}
```

**Indexes:**

- `{userId: 1}` - Unique profile per user
- `{totalOrders: -1}` - Most active customers
- `{totalSpent: -1}` - Best customers by spending

**Purpose:**

- Store customer contact and shipping info
- Track purchase history statistics
- Display customer dashboard and preferences

---

### 7. seller_profiles ⭐ NEW

Extended seller/store information.

```
{
  _id: ObjectId,
  sellerId: ObjectId (unique),
  storeName: string,
  description: string,
  phone: string,
  businessAddress: string,
  city: string,
  postalCode: string,
  country: string,
  businessLicense: string,
  totalProductsSold: number,
  totalRevenue: decimal,
  averageOrderValue: decimal,
  averageRating: decimal,
  totalReviews: number,
  bestSellingProductIds: [ObjectId],
  topRatedProductIds: [ObjectId],
  logoMediaId: ObjectId,
  bannerMediaId: ObjectId,
  isActive: boolean,
  lastOrderDate: datetime,
  createdAt: datetime,
  updatedAt: datetime
}
```

**Indexes:**

- `{sellerId: 1}` - Unique profile per seller
- `{totalRevenue: -1}` - Top sellers by revenue
- `{averageRating: -1}` - Top rated sellers
- `{totalProductsSold: -1}` - Best sellers by volume
- `{isActive: 1}` - Active sellers

**Purpose:**

- Store seller store information
- Track sales and revenue
- Display seller dashboard and ratings
- List best-selling and top-rated products

---

## Relationships

```
users (1) ─── many ─── orders
           └─ many ─── shopping_carts
           └─ one ──── user_profiles
           └─ one ──── seller_profiles (if SELLER role)

products (1) ─── many ─── orders (via items)
          └─ many ─── shopping_carts (via items)

orders (1) ─── many ─── order_items (embedded)
          └─ many ─── seller_profiles (via items.sellerId)
          └─ many ─── user_profiles (via buyerId)

shopping_carts (1) ─── many ─── cart_items (embedded)
```

---

## Key Design Decisions

### 1. Embedded vs Referenced

- ✅ **Embedded**: OrderItem, CartItem (always queried with parent)
- ✅ **Referenced**: Product, User, Media (can be queried independently)

### 2. Denormalization

- `productName`, `price` in OrderItem/CartItem for history
- Seller info denormalized in order items
- Stats (totalOrders, totalSpent) in profiles for fast queries

### 3. Compound Indexes

- Optimize common queries: `{buyerId, createdAt}`, `{status, createdAt}`
- Support sorting and filtering simultaneously

### 4. TTL Indexes (Future)

- Automatic cart cleanup after 90 days of inactivity
- Reduces storage costs

### 5. Atomicity

- Single-document transactions for cart/order operations
- Multi-document transactions (MongoDB 4.0+) for complex operations

---

## Data Validation

### Orders

- `totalPrice` ≥ 0
- `items` not empty
- `status` in defined enum
- `paymentMethod` in defined enum

### Shopping Cart

- `totalPrice` = sum(items[*].subtotal)
- `items.quantity` ≥ 1
- `items.price` > 0

### User Profile

- `userId` unique
- `totalOrders` ≥ 0
- `totalSpent` ≥ 0

### Seller Profile

- `sellerId` unique
- `totalProductsSold` ≥ 0
- `averageRating` between 0 and 5

---

## Migrations

### Initial Setup

```javascript
// Create collections with validators
db.createCollection("orders", {
  validator: {
    $jsonSchema: {
      /* schema */
    },
  },
});

// Create indexes
db.orders.createIndex({ buyerId: 1, createdAt: -1 });
db.shopping_carts.createIndex({ userId: 1 }, { unique: true });
db.user_profiles.createIndex({ userId: 1 }, { unique: true });
db.seller_profiles.createIndex({ sellerId: 1 }, { unique: true });
```

---

## Performance Considerations

### Query Optimization

| Query                 | Index                  | Expected Time |
| --------------------- | ---------------------- | ------------- |
| Find orders by buyer  | `{buyerId, createdAt}` | <10ms         |
| Find orders by status | `{status, createdAt}`  | <20ms         |
| Find user cart        | `{userId}` unique      | <5ms          |
| Search products       | `{name}`               | <50ms         |

### Storage

| Collection      | Avg Doc Size | Est. Storage (1M docs) |
| --------------- | ------------ | ---------------------- |
| orders          | 500 bytes    | 500 MB                 |
| shopping_carts  | 1 KB         | 1 GB                   |
| user_profiles   | 300 bytes    | 300 MB                 |
| seller_profiles | 400 bytes    | 400 MB                 |

---

## Backup & Recovery

- Full backup: Daily snapshots
- Transaction logs: Enabled for recovery
- Point-in-time restore: 7-day window

---

## Future Enhancements

- [ ] Add `reviews` collection for product ratings
- [x] Wishlist stored in `user_profiles.favoriteProductIds`
- [ ] Add `payments` collection for detailed payment tracking
- [ ] Implement sharding for scaling (collection > 500MB)
- [ ] Add audit logs for compliance
