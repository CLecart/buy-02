import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Product, Page } from "../models/product.model";

/**
 * Product service for CRUD operations.
 */
@Injectable({
  providedIn: "root",
})
export class ProductService {
  private readonly API_URL = "/api/products";

  constructor(private http: HttpClient) {}

  /**
   * Get all products with pagination.
   */
  getProducts(
    page: number = 0,
    size: number = 10,
    search?: string
  ): Observable<Page<Product>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (search) {
      params = params.set("search", search);
    }

    return this.http.get<Page<Product>>(this.API_URL, { params });
  }

  /**
   * Get a product by ID.
   */
  getProduct(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.API_URL}/${id}`);
  }

  /**
   * Create a new product (SELLER only).
   */
  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.API_URL, product);
  }

  /**
   * Update an existing product (SELLER only, must be owner).
   */
  updateProduct(id: string, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.API_URL}/${id}`, product);
  }

  /**
   * Delete a product (SELLER only, must be owner).
   */
  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
