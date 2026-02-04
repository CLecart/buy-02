/**
 * Product model representing a seller's product.
 */
export interface Product {
  id?: string;
  name: string;
  description?: string;
  price: number;
  quantity?: number;
  ownerId?: string;
  mediaIds?: string[];
}

/**
 * Paginated response from the API.
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
