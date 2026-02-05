import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { OrderService } from "../../services/order.service";
import { AuthService } from "../../services/auth.service";
import { Order, OrderStatus } from "../../models/order.model";
import { User } from "../../models/user.model";
import { Page } from "../../models/product.model";

@Component({
  selector: "app-orders",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./orders.component.html",
  styleUrls: ["./orders.component.scss"],
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  currentUser: User | null = null;
  viewMode: "buyer" | "seller" = "buyer";
  searchTerm = "";
  statusFilter: OrderStatus | "" = "";
  currentPage = 0;
  totalPages = 0;
  pageSize = 8;
  statusUpdates: Record<string, OrderStatus> = {};
  trackingUpdates: Record<string, string> = {};
  statusReasons: Record<string, string> = {};

  readonly orderStatuses: OrderStatus[] = [
    "PENDING",
    "CONFIRMED",
    "SHIPPED",
    "DELIVERED",
    "CANCELLED",
    "RETURNED",
    "REFUNDED",
  ];

  constructor(
    private readonly orderService: OrderService,
    public authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (user?.role === "SELLER") {
        this.viewMode = "seller";
      }
    });
    this.loadOrders();
  }

  loadOrders(): void {
    const status = this.statusFilter || undefined;

    const handler =
      this.viewMode === "seller"
        ? this.orderService.getSellerOrders(
            this.currentPage,
            this.pageSize,
            this.searchTerm || undefined,
            status,
          )
        : this.orderService.getMyOrders(
            this.currentPage,
            this.pageSize,
            this.searchTerm || undefined,
            status,
          );

    handler.subscribe({
      next: (page: Page<Order>) => {
        this.orders = page.content;
        this.totalPages = page.totalPages;
      },
      error: (err: unknown) => console.error("Failed to load orders", err),
    });
  }

  switchMode(mode: "buyer" | "seller"): void {
    this.viewMode = mode;
    this.currentPage = 0;
    this.loadOrders();
  }

  search(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  loadPage(page: number): void {
    this.currentPage = page;
    this.loadOrders();
  }

  cancelOrder(orderId: string): void {
    this.orderService.cancelOrder(orderId).subscribe({
      next: () => this.loadOrders(),
      error: (err: unknown) => console.error("Failed to cancel order", err),
    });
  }

  redoOrder(orderId: string): void {
    this.orderService.redoOrder(orderId).subscribe({
      next: () => this.loadOrders(),
      error: (err: unknown) => console.error("Failed to redo order", err),
    });
  }

  deleteOrder(orderId: string): void {
    this.orderService.deleteOrder(orderId).subscribe({
      next: () => this.loadOrders(),
      error: (err: unknown) => console.error("Failed to delete order", err),
    });
  }

  updateStatus(orderId: string): void {
    const status = this.statusUpdates[orderId];
    if (!status) {
      return;
    }

    this.orderService.updateOrderStatus(orderId, { status }).subscribe({
      next: () => this.loadOrders(),
      error: (err: unknown) => console.error("Failed to update status", err),
    });
  }

  updateStatusWithDetails(orderId: string): void {
    const status = this.statusUpdates[orderId];
    if (!status) {
      return;
    }

    const trackingNumber = this.trackingUpdates[orderId];
    const reason = this.statusReasons[orderId];

    this.orderService
      .updateOrderStatus(orderId, {
        status,
        trackingNumber: trackingNumber?.trim() || undefined,
        reason: reason?.trim() || undefined,
      })
      .subscribe({
        next: () => this.loadOrders(),
        error: (err: unknown) => console.error("Failed to update status", err),
      });
  }

  isCancellable(order: Order): boolean {
    return order.status !== "CANCELLED" && order.status !== "DELIVERED";
  }
}
