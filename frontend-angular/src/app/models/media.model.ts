export interface Media {
  id: string;
  ownerId: string;
  productId?: string;
  filename: string;
  originalName?: string;
  mimeType: string;
  size: number;
  checksum?: string;
  uploadedAt: string;
  width?: number;
  height?: number;
  url?: string;
}

export interface MediaUploadResponse {
  filename: string;
  url: string;
}
