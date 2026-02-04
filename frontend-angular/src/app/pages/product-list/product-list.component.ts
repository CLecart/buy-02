import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { ProductService } from "../../services/product.service";
import { AuthService } from "../../services/auth.service";
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
  currentPage = 0;
  totalPages = 0;
  pageSize = 12;

  constructor(
    private productService: ProductService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService
      .getProducts(
        this.currentPage,
        this.pageSize,
        this.searchTerm || undefined
      )
      .subscribe({
        next: (page: Page<Product>) => {
          this.products = page.content;
          this.totalPages = page.totalPages;
        },
        error: (err: unknown) => console.error("Failed to load products", err),
      });
  }

  search(): void {
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
}
