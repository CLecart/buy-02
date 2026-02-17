# Wishlist Feature - buy-02 E-Commerce Platform

## Collection: wishlists

```
{
  _id: ObjectId,
  userId: ObjectId (unique),
  items: [
    {
      productId: ObjectId,
      productName: string,
      price: decimal,
      mediaId: ObjectId,
      addedAt: datetime
    }
  ],
  createdAt: datetime,
  updatedAt: datetime
}
```

**Indexes:**

- `{userId: 1}` - Unique wishlist per user

**Purpose:**

- Allow users to save products for future purchase
- Support wishlist management (add/remove/view)

---

## API Endpoints (to implement)

- `GET /api/wishlist` - Get current user's wishlist
- `POST /api/wishlist/add` - Add product to wishlist
- `POST /api/wishlist/remove` - Remove product from wishlist
- `POST /api/wishlist/clear` - Clear wishlist

---

## Frontend UI (to implement)

- Wishlist page/component
- Add to wishlist button on product cards
- Remove from wishlist button
- Move item from wishlist to cart

---

## Tests (to implement)

- Unit tests for wishlist service/model
- Integration tests for wishlist API
- UI tests for wishlist component

---

## Documentation

- Update README.md with wishlist feature
- Document API and UI usage
