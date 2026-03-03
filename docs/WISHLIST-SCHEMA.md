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

## API Endpoints (implemented)

- `GET /api/wishlist` - Get current user's wishlist
- `POST /api/wishlist/add` - Add product to wishlist
- `POST /api/wishlist/remove` - Remove product from wishlist
- `POST /api/wishlist/clear` - Clear wishlist
- `POST /api/profiles/users/me/favorites/{productId}` - Add favorite (user profile side)
- `DELETE /api/profiles/users/me/favorites/{productId}` - Remove favorite (user profile side)

---

## Frontend UI (implemented)

- Wishlist page/component: `frontend-angular/src/app/pages/wishlist`
- Wishlist service: `frontend-angular/src/app/services/wishlist.service.ts`
- Unit test: `frontend-angular/src/app/services/wishlist.service.spec.ts`
- Integration with product browsing and user interactions is available via frontend services/routes.

---

## Tests (implemented)

- Backend integration test: `product-service/src/test/java/com/example/productservice/WishlistServiceIntegrationTest.java`
- Frontend unit test: `frontend-angular/src/app/services/wishlist.service.spec.ts`
- General backend/frontend CI pipelines validate non-regression for this feature set.

---

## Documentation

- README includes wishlist as bonus feature and related API usage.
- Audit documentation includes wishlist schema and evidence references.
