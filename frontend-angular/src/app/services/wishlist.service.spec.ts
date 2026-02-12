import { TestBed } from "@angular/core/testing";
import {
  HttpClientTestingModule,
  HttpTestingController,
} from "@angular/common/http/testing";
import { WishlistService, Wishlist } from "./wishlist.service";

const mockWishlist: Wishlist = {
  userId: "user1",
  items: [
    {
      productId: "p1",
      productName: "Product 1",
      price: 10,
      addedAt: "2024-01-01T00:00:00Z",
    },
    {
      productId: "p2",
      productName: "Product 2",
      price: 20,
      addedAt: "2024-01-02T00:00:00Z",
    },
  ],
};

describe("WishlistService", () => {
  let service: WishlistService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [WishlistService],
    });
    service = TestBed.inject(WishlistService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it("should get wishlist", () => {
    service.getWishlist().subscribe((data) => {
      expect(data).toEqual(mockWishlist);
    });
    const req = http.expectOne("/api/wishlist");
    expect(req.request.method).toBe("GET");
    req.flush(mockWishlist);
  });

  it("should add to wishlist", () => {
    service.addToWishlist("p3").subscribe((res) => {
      expect(res.success).toBeTrue();
    });
    const req = http.expectOne("/api/wishlist/add");
    expect(req.request.method).toBe("POST");
    expect(req.request.body).toEqual({ productId: "p3" });
    req.flush({ success: true });
  });

  it("should remove from wishlist", () => {
    service.removeFromWishlist("p1").subscribe((res) => {
      expect(res.success).toBeTrue();
    });
    const req = http.expectOne("/api/wishlist/remove");
    expect(req.request.method).toBe("POST");
    expect(req.request.body).toEqual({ productId: "p1" });
    req.flush({ success: true });
  });

  it("should clear wishlist", () => {
    service.clearWishlist().subscribe((res) => {
      expect(res).toBeUndefined();
    });
    const req = http.expectOne("/api/wishlist/clear");
    expect(req.request.method).toBe("POST");
    expect(req.request.body).toEqual({});
    req.flush(null);
  });
});
