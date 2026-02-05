import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ProfileService } from "../../services/profile.service";
import { AuthService } from "../../services/auth.service";
import { UserProfile, SellerProfile } from "../../models/profile.model";
import { User } from "../../models/user.model";
import { forkJoin } from "rxjs";
import { Product } from "../../models/product.model";
import { ProductService } from "../../services/product.service";

@Component({
  selector: "app-profile",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./profile.component.html",
  styleUrls: ["./profile.component.scss"],
})
export class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  userProfile: UserProfile | null = null;
  sellerProfile: SellerProfile | null = null;
  favoriteProducts: Product[] = [];
  mostPurchasedProducts: Product[] = [];
  bestSellingProducts: Product[] = [];

  constructor(
    private readonly profileService: ProfileService,
    private readonly authService: AuthService,
    private readonly productService: ProductService,
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (user) {
        this.loadProfiles(user);
      } else {
        this.userProfile = null;
        this.sellerProfile = null;
        this.favoriteProducts = [];
        this.mostPurchasedProducts = [];
        this.bestSellingProducts = [];
      }
    });
  }

  private loadProfiles(user: User): void {
    this.profileService.getMyProfile().subscribe({
      next: (profile) => {
        this.userProfile = profile;
        this.loadUserProductDetails(profile);
      },
      error: (err: unknown) =>
        console.error("Failed to load user profile", err),
    });

    if (user.role === "SELLER") {
      this.profileService.getSellerProfile(user.id).subscribe({
        next: (profile) => {
          this.sellerProfile = profile;
          this.loadSellerProductDetails(profile);
        },
        error: (err: unknown) =>
          console.error("Failed to load seller profile", err),
      });
    }
  }

  removeFavorite(productId: string): void {
    this.profileService.removeFavorite(productId).subscribe({
      next: () => {
        if (this.userProfile?.favoriteProductIds) {
          this.userProfile.favoriteProductIds =
            this.userProfile.favoriteProductIds.filter(
              (id) => id !== productId,
            );
        }
        this.favoriteProducts = this.favoriteProducts.filter(
          (product) => product.id !== productId,
        );
      },
      error: (err: unknown) => console.error("Failed to remove favorite", err),
    });
  }

  private loadUserProductDetails(profile: UserProfile): void {
    const favoriteIds = profile.favoriteProductIds ?? [];
    const mostPurchasedIds = profile.mostPurchasedProductIds ?? [];

    this.loadProducts(favoriteIds, (products) => {
      this.favoriteProducts = products;
    });

    this.loadProducts(mostPurchasedIds, (products) => {
      this.mostPurchasedProducts = products;
    });
  }

  private loadSellerProductDetails(profile: SellerProfile): void {
    const bestSellingIds = profile.bestSellingProductIds ?? [];
    this.loadProducts(bestSellingIds, (products) => {
      this.bestSellingProducts = products;
    });
  }

  private loadProducts(
    ids: string[],
    handler: (products: Product[]) => void,
  ): void {
    if (!ids.length) {
      handler([]);
      return;
    }

    const productRequests = ids.map((id) => this.productService.getProduct(id));

    forkJoin(productRequests).subscribe({
      next: (products) => {
        handler(products.filter((p): p is Product => Boolean(p)));
      },
      error: (err: unknown) =>
        console.error("Failed to load product details", err),
    });
  }
}
