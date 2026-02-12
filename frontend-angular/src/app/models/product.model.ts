export interface Product {
  id?: string;
  name: string;
  description?: string;
  category?: string;
  price: number;
  quantity?: number;
  ownerId?: string;
  mediaIds?: string[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
