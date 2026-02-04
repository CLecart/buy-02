import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Media, MediaUploadResponse } from "../models/media.model";
import { Page } from "../models/product.model";

/**
 * Media service for upload and management operations.
 */
@Injectable({
  providedIn: "root",
})
export class MediaService {
  private readonly API_URL = "/api/media";

  /** Maximum file size in bytes (2MB). */
  public static readonly MAX_FILE_SIZE = 2 * 1024 * 1024;

  /** Allowed MIME types for upload. */
  public static readonly ALLOWED_MIME_TYPES = [
    "image/jpeg",
    "image/png",
    "image/gif",
  ];

  constructor(private http: HttpClient) {}

  /**
   * Upload media for a product (SELLER only).
   * @param file The image file to upload
   * @param productId The product to associate the media with
   */
  uploadMedia(file: File, productId: string): Observable<MediaUploadResponse> {
    // Client-side validation
    if (file.size > MediaService.MAX_FILE_SIZE) {
      throw new Error("File size exceeds 2MB limit");
    }
    if (!MediaService.ALLOWED_MIME_TYPES.includes(file.type)) {
      throw new Error("Only JPEG, PNG, and GIF images are allowed");
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("productId", productId);

    return this.http.post<MediaUploadResponse>(
      `${this.API_URL}/upload`,
      formData
    );
  }

  /**
   * Get media list with pagination.
   */
  getMedia(
    page: number = 0,
    size: number = 20,
    productId?: string
  ): Observable<Page<Media>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (productId) {
      params = params.set("productId", productId);
    }

    return this.http.get<Page<Media>>(this.API_URL, { params });
  }

  /**
   * Get media by ID.
   */
  getMediaById(id: string): Observable<Media> {
    return this.http.get<Media>(`${this.API_URL}/${id}`);
  }

  /**
   * Delete media (SELLER only, must be owner).
   */
  deleteMedia(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Validate file before upload.
   * @returns Error message or null if valid
   */
  validateFile(file: File): string | null {
    if (file.size > MediaService.MAX_FILE_SIZE) {
      return "File size exceeds 2MB limit";
    }
    if (!MediaService.ALLOWED_MIME_TYPES.includes(file.type)) {
      return "Only JPEG, PNG, and GIF images are allowed";
    }
    return null;
  }
}
