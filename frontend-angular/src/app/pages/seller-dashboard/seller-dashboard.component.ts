import { Component, HostListener, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { ProductService } from "../../services/product.service";
import { MediaService } from "../../services/media.service";
import { Media } from "../../models/media.model";
import { AuthService } from "../../services/auth.service";
import { Product, Page } from "../../models/product.model";
import { User } from "../../models/user.model";
import { ProfileService } from "../../services/profile.service";
import { SellerProfile } from "../../models/profile.model";

@Component({
  selector: "app-seller-dashboard",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./seller-dashboard.component.html",
  styleUrls: ["./seller-dashboard.component.scss"],
})
export class SellerDashboardComponent implements OnInit {
  currentUser: User | null = null;
  sellerProfile: SellerProfile | null = null;
  myProducts: Product[] = [];
  productForm: Product = {
    name: "",
    price: 0,
    description: "",
    quantity: 0,
    category: "",
  };
  editingProduct: Product | null = null;

  selectedProductForMedia: Product | null = null;
  productMedia: Media[] = [];
  uploadError = "";

  constructor(
    private readonly productService: ProductService,
    private readonly mediaService: MediaService,
    private readonly authService: AuthService,
    private readonly profileService: ProfileService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user: User | null) => {
      this.currentUser = user;
      if (!user) {
        this.myProducts = [];
        this.sellerProfile = null;
        return;
      }
      if (user.role !== "SELLER") {
        this.router.navigate(["/"]);
        return;
      }
      this.loadMyProducts();
      this.loadSellerProfile();
    });
  }

  getInitials(): string {
    return this.currentUser?.name?.charAt(0).toUpperCase() || "?";
  }

  loadMyProducts(): void {
    if (!this.currentUser?.id) {
      return;
    }
    this.productService
      .getProducts(0, 100, { sellerId: this.currentUser?.id })
      .subscribe({
        next: (page: Page<Product>) => {
          this.myProducts = page.content.filter(
            (p: Product) => p.ownerId === this.currentUser?.id,
          );
        },
      });
  }

  loadSellerProfile(): void {
    if (!this.currentUser?.id) {
      return;
    }
    this.profileService.getSellerProfile(this.currentUser.id).subscribe({
      next: (profile) => (this.sellerProfile = profile),
      error: (err: unknown) =>
        console.error("Failed to load seller profile", err),
    });
  }

  requestVerification(): void {
    if (!this.currentUser?.id || this.sellerProfile?.verified) {
      return;
    }
    this.profileService.verifySeller(this.currentUser.id).subscribe({
      next: () => this.loadSellerProfile(),
      error: (err: { error?: { message?: string }; message?: string }) =>
        alert(
          "Failed to verify seller: " + (err.error?.message || err.message),
        ),
    });
  }

  saveProduct(): void {
    if (this.editingProduct?.id) {
      this.productService
        .updateProduct(this.editingProduct.id, this.productForm)
        .subscribe({
          next: () => {
            this.loadMyProducts();
            this.cancelEdit();
          },
          error: (err: { error?: { message?: string }; message?: string }) =>
            alert(
              "Failed to update product: " +
                (err.error?.message || err.message),
            ),
        });
    } else {
      this.productService.createProduct(this.productForm).subscribe({
        next: () => {
          this.loadMyProducts();
          this.productForm = {
            name: "",
            price: 0,
            description: "",
            quantity: 0,
            category: "",
          };
        },
        error: (err: { error?: { message?: string }; message?: string }) =>
          alert(
            "Failed to create product: " + (err.error?.message || err.message),
          ),
      });
    }
  }

  editProduct(product: Product): void {
    this.editingProduct = product;
    this.productForm = { ...product };
  }

  cancelEdit(): void {
    this.editingProduct = null;
    this.productForm = {
      name: "",
      price: 0,
      description: "",
      quantity: 0,
      category: "",
    };
  }

  deleteProduct(product: Product): void {
    if (confirm(`Delete "${product.name}"?`)) {
      this.productService.deleteProduct(product.id!).subscribe({
        next: () => this.loadMyProducts(),
        error: (err: { error?: { message?: string }; message?: string }) =>
          alert(
            "Failed to delete product: " + (err.error?.message || err.message),
          ),
      });
    }
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      const error = this.mediaService.validateFile(file);
      if (error) {
        alert(error);
        return;
      }
      this.authService.uploadAvatar(file).subscribe({
        error: (err: { error?: { message?: string }; message?: string }) =>
          alert(
            "Failed to upload avatar: " + (err.error?.message || err.message),
          ),
      });
    }
  }

  openMediaManager(product: Product): void {
    this.selectedProductForMedia = product;
    this.uploadError = "";
    this.loadProductMedia();
  }

  closeMediaManager(): void {
    this.selectedProductForMedia = null;
    this.productMedia = [];
  }

  @HostListener("document:keydown.escape")
  handleEscape(): void {
    if (this.selectedProductForMedia) {
      this.closeMediaManager();
    }
  }

  loadProductMedia(): void {
    if (!this.selectedProductForMedia?.id) return;
    this.mediaService
      .getMedia(0, 50, this.selectedProductForMedia.id)
      .subscribe({
        next: (page: Page<Media>) => (this.productMedia = page.content),
      });
  }

  onMediaSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !this.selectedProductForMedia?.id) return;

    this.uploadError = "";
    const files = Array.from(input.files);

    for (const file of files) {
      const error = this.mediaService.validateFile(file);
      if (error) {
        this.uploadError = error;
        continue;
      }

      this.mediaService
        .uploadMedia(file, this.selectedProductForMedia.id)
        .subscribe({
          next: () => this.loadProductMedia(),
          error: (err: { error?: { message?: string } }) =>
            (this.uploadError = err.error?.message || "Upload failed"),
        });
    }

    input.value = "";
  }

  deleteMedia(mediaId: string): void {
    this.mediaService.deleteMedia(mediaId).subscribe({
      next: () => this.loadProductMedia(),
      error: (err: { error?: { message?: string }; message?: string }) =>
        alert("Failed to delete media: " + (err.error?.message || err.message)),
    });
  }
}
