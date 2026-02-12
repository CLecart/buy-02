import { Component, OnInit } from "@angular/core";
import { WishlistService, Wishlist } from "../../services/wishlist.service";

@Component({
  selector: "app-wishlist",
  templateUrl: "./wishlist.component.html",
  styleUrls: ["./wishlist.component.scss"],
})
export class WishlistComponent implements OnInit {
  wishlist: Wishlist | null = null;
  loading = false;

  constructor(private readonly wishlistService: WishlistService) {}

  ngOnInit(): void {
    this.loadWishlist();
  }

  loadWishlist(): void {
    this.loading = true;
    this.wishlistService.getWishlist().subscribe({
      next: (data) => {
        this.wishlist = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  removeItem(productId: string): void {
    this.wishlistService.removeFromWishlist(productId).subscribe(() => {
      this.loadWishlist();
    });
  }

  clearWishlist(): void {
    this.wishlistService.clearWishlist().subscribe(() => {
      this.loadWishlist();
    });
  }
}
