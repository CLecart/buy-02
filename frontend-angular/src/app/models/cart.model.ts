export interface CartItem {
  productId: string;
  sellerId: string;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
  createdAt?: string;
}

export interface ShoppingCart {
  id: string;
  userId: string;
  items: CartItem[];
  totalPrice: number;
  itemCount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AddToCartRequest {
  productId: string;
  sellerId: string;
  productName: string;
  quantity: number;
  price: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}
