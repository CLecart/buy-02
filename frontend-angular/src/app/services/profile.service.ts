import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { SellerProfile, UserProfile } from "../models/profile.model";

@Injectable({
  providedIn: "root",
})
export class ProfileService {
  private readonly USER_API = "/api/profiles/users";
  private readonly SELLER_API = "/api/profiles/sellers";

  constructor(private readonly http: HttpClient) {}

  getUserProfile(userId: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.USER_API}/${userId}`);
  }

  getMyProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.USER_API}/me`);
  }

  addFavorite(productId: string): Observable<void> {
    return this.http.post<void>(
      `${this.USER_API}/me/favorites/${productId}`,
      {},
    );
  }

  removeFavorite(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.USER_API}/me/favorites/${productId}`);
  }

  getSellerProfile(sellerId: string): Observable<SellerProfile> {
    return this.http.get<SellerProfile>(`${this.SELLER_API}/${sellerId}`);
  }
}
