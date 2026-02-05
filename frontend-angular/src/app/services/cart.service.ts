import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  ShoppingCart,
  AddToCartRequest,
  UpdateCartItemRequest,
} from "../models/cart.model";

@Injectable({
  providedIn: "root",
})
export class CartService {
  private readonly API_URL = "/api/carts/me";

  constructor(private readonly http: HttpClient) {}

  getCart(): Observable<ShoppingCart> {
    return this.http.get<ShoppingCart>(this.API_URL);
  }

  addItem(request: AddToCartRequest): Observable<ShoppingCart> {
    return this.http.post<ShoppingCart>(`${this.API_URL}/items`, request);
  }

  updateItem(
    productId: string,
    request: UpdateCartItemRequest,
  ): Observable<ShoppingCart> {
    return this.http.patch<ShoppingCart>(
      `${this.API_URL}/items/${productId}`,
      request,
    );
  }

  removeItem(productId: string): Observable<ShoppingCart> {
    return this.http.delete<ShoppingCart>(`${this.API_URL}/items/${productId}`);
  }

  clearCart(): Observable<ShoppingCart> {
    return this.http.delete<ShoppingCart>(this.API_URL);
  }

  deleteCart(): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/delete`);
  }
}
