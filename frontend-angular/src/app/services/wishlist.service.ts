import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface WishlistItem {
  productId: string;
  productName: string;
  price: number;
  mediaId?: string;
  addedAt: string;
}

export interface Wishlist {
  userId: string;
  items: WishlistItem[];
}

@Injectable({ providedIn: "root" })
export class WishlistService {
  constructor(private readonly http: HttpClient) {}

  getWishlist(): Observable<Wishlist> {
    return this.http.get<Wishlist>("/api/wishlist");
  }

  addToWishlist(productId: string): Observable<{ success: boolean }> {
    return this.http.post<{ success: boolean }>("/api/wishlist/add", {
      productId,
    });
  }

  removeFromWishlist(productId: string): Observable<{ success: boolean }> {
    return this.http.post<{ success: boolean }>("/api/wishlist/remove", {
      productId,
    });
  }

  clearWishlist(): Observable<void> {
    return this.http.post<void>("/api/wishlist/clear", {});
  }
}
