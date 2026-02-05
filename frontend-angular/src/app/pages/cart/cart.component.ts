import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { CartService } from "../../services/cart.service";
import { OrderService } from "../../services/order.service";
import { AuthService } from "../../services/auth.service";
import { ShoppingCart } from "../../models/cart.model";
import { CreateOrderRequest, PaymentMethod } from "../../models/order.model";
import { User } from "../../models/user.model";

@Component({
  selector: "app-cart",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./cart.component.html",
  styleUrls: ["./cart.component.scss"],
})
export class CartComponent implements OnInit {
  cart: ShoppingCart | null = null;
  currentUser: User | null = null;
  shippingAddress = "";
  paymentMethod: PaymentMethod = "PAY_ON_DELIVERY";
  paymentReference = "";
  isSubmitting = false;

  constructor(
    private readonly cartService: CartService,
    private readonly orderService: OrderService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });
    this.loadCart();
  }

  loadCart(): void {
    if (!this.authService.isAuthenticated()) {
      this.cart = null;
      return;
    }

    this.cartService.getCart().subscribe({
      next: (cart) => (this.cart = cart),
      error: (err: unknown) => console.error("Failed to load cart", err),
    });
  }

  updateQuantity(productId: string, quantity: number): void {
    if (quantity < 1) {
      return;
    }

    this.cartService.updateItem(productId, { quantity }).subscribe({
      next: (cart) => (this.cart = cart),
      error: (err: unknown) => console.error("Failed to update cart", err),
    });
  }

  removeItem(productId: string): void {
    this.cartService.removeItem(productId).subscribe({
      next: (cart) => (this.cart = cart),
      error: (err: unknown) => console.error("Failed to remove item", err),
    });
  }

  clearCart(): void {
    this.cartService.clearCart().subscribe({
      next: (cart) => (this.cart = cart),
      error: (err: unknown) => console.error("Failed to clear cart", err),
    });
  }

  checkout(): void {
    if (!this.cart || !this.currentUser) {
      return;
    }

    if (!this.shippingAddress.trim()) {
      alert("Shipping address is required.");
      return;
    }

    if (
      this.paymentMethod !== "PAY_ON_DELIVERY" &&
      !this.paymentReference.trim()
    ) {
      alert("Payment reference is required for the selected method.");
      return;
    }

    const request: CreateOrderRequest = {
      buyerId: this.currentUser.id,
      buyerEmail: this.currentUser.email,
      items: this.cart.items.map((item) => ({
        productId: item.productId,
        sellerId: item.sellerId,
        productName: item.productName,
        quantity: item.quantity,
        price: item.price,
      })),
      paymentMethod: this.paymentMethod,
      paymentReference: this.paymentReference.trim() || undefined,
      shippingAddress: this.shippingAddress.trim(),
    };

    this.isSubmitting = true;
    this.orderService.createOrder(request).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.shippingAddress = "";
        this.paymentMethod = "PAY_ON_DELIVERY";
        this.paymentReference = "";
        this.clearCart();
        alert("Order placed successfully.");
      },
      error: (err: unknown) => {
        this.isSubmitting = false;
        console.error("Failed to place order", err);
      },
    });
  }
}
