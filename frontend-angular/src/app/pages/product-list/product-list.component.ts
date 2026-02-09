import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { ProductService } from "../../services/product.service";
import { CartService } from "../../services/cart.service";
import { AuthService } from "../../services/auth.service";
import { ProfileService } from "../../services/profile.service";
import { MediaService } from "../../services/media.service";
import { Product, Page } from "../../models/product.model";

/**
 * Product listing page - accessible to all users.
 */
@Component({
  selector: "app-product-list",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./product-list.component.html",
  styleUrls: ["./product-list.component.scss"],
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  searchTerm = "";
  category = "";
  minPrice?: number;
  maxPrice?: number;
  inStockOnly = false;
  sellerId = "";
  currentPage = 0;
  totalPages = 0;
  pageSize = 12;
  favoriteIds = new Set<string>();
  mediaUrls: Record<string, string> = {};

  constructor(
    private readonly productService: ProductService,
    private readonly cartService: CartService,
    public authService: AuthService,
    private readonly profileService: ProfileService,
    private readonly mediaService: MediaService,
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.authService.currentUser$.subscribe((user) => {
      if (!user) {
        this.favoriteIds.clear();
        return;
      }
      this.profileService.getMyProfile().subscribe({
        next: (profile) => {
          this.favoriteIds = new Set(profile.favoriteProductIds ?? []);
        },
        error: (err: unknown) => console.error("Failed to load favorites", err),
      });
    });
  }

  loadProducts(): void {
    this.productService
      .getProducts(this.currentPage, this.pageSize, {
        search: this.searchTerm || undefined,
        category: this.category || undefined,
        minPrice: this.minPrice,
        maxPrice: this.maxPrice,
        sellerId: this.sellerId || undefined,
        inStock: this.inStockOnly ? true : undefined,
      })
      .subscribe({
        next: (page: Page<Product>) => {
          this.products = page.content;
          this.totalPages = page.totalPages;
          this.resolveMediaUrls(page.content);
        },
        error: (err: unknown) => console.error("Failed to load products", err),
      });
  }

  search(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  resetFilters(): void {
    this.searchTerm = "";
    this.category = "";
    this.minPrice = undefined;
    this.maxPrice = undefined;
    this.inStockOnly = false;
    this.sellerId = "";
    this.currentPage = 0;
    this.loadProducts();
  }

  loadPage(page: number): void {
    this.currentPage = page;
    this.loadProducts();
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).style.display = "none";
  }

  private resolveMediaUrls(products: Product[]): void {
    for (const product of products) {
      const productId = product.id;
      const mediaId = product.mediaIds?.[0];
      if (!productId || !mediaId || this.mediaUrls[productId]) {
        continue;
      }
      this.mediaService.getMediaById(mediaId).subscribe({
        next: (media) => {
          const url =
            media.url || `/api/media/files/${media.ownerId}/${media.filename}`;
          this.mediaUrls[productId] = url;
        },
        error: (err: unknown) => console.error("Failed to load media", err),
      });
    }
  }

  addToCart(product: Product): void {
    if (!this.authService.isAuthenticated()) {
      alert("Please sign in to add items to your cart.");
      return;
    }

    if (!product.id || !product.ownerId) {
      return;
    }

    this.cartService
      .addItem({
        productId: product.id,
        sellerId: product.ownerId,
        productName: product.name,
        quantity: 1,
        price: product.price,
      })
      .subscribe({
        next: () => alert("Added to cart."),
        error: (err: unknown) => console.error("Failed to add to cart", err),
      });
  }

  isFavorite(productId?: string): boolean {
    if (!productId) {
      return false;
    }
    return this.favoriteIds.has(productId);
  }

  toggleFavorite(product: Product): void {
    if (!this.authService.isAuthenticated()) {
      alert("Please sign in to manage your wishlist.");
      return;
    }

    if (!product.id) {
      return;
    }

    if (this.isFavorite(product.id)) {
      this.profileService.removeFavorite(product.id).subscribe({
        next: () => this.favoriteIds.delete(product.id as string),
        error: (err: unknown) =>
          console.error("Failed to remove favorite", err),
      });
    } else {
      this.profileService.addFavorite(product.id).subscribe({
        next: () => this.favoriteIds.add(product.id as string),
        error: (err: unknown) => console.error("Failed to add favorite", err),
      });
    }
  }
}
