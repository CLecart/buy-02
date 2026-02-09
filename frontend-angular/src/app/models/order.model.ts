export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED"
  | "RETURNED"
  | "REFUNDED";

export type PaymentMethod = "PAY_ON_DELIVERY" | "PAYPAL" | "WALLET";

export interface OrderItem {
  productId: string;
  sellerId: string;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface Order {
  id: string;
  buyerId: string;
  buyerEmail?: string;
  items: OrderItem[];
  totalPrice: number;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  paymentReference?: string;
  shippingAddress: string;
  trackingNumber?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateOrderRequestItem {
  productId: string;
  sellerId: string;
  productName: string;
  quantity: number;
  price: number;
}

export interface CreateOrderRequest {
  buyerId: string;
  buyerEmail: string;
  items: CreateOrderRequestItem[];
  paymentMethod: PaymentMethod;
  paymentReference?: string;
  shippingAddress: string;
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
  trackingNumber?: string;
  reason?: string;
}
