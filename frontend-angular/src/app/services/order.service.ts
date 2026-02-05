import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  CreateOrderRequest,
  Order,
  OrderStatus,
  UpdateOrderStatusRequest,
} from "../models/order.model";
import { Page } from "../models/product.model";

@Injectable({
  providedIn: "root",
})
export class OrderService {
  private readonly API_URL = "/api/orders";

  constructor(private readonly http: HttpClient) {}

  createOrder(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.API_URL, request);
  }

  getMyOrders(
    page: number,
    size: number,
    search?: string,
    status?: OrderStatus,
  ): Observable<Page<Order>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (search) {
      params = params.set("search", search);
    }

    if (status) {
      params = params.set("status", status);
    }

    return this.http.get<Page<Order>>(`${this.API_URL}/me`, { params });
  }

  getSellerOrders(
    page: number,
    size: number,
    search?: string,
    status?: OrderStatus,
  ): Observable<Page<Order>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (search) {
      params = params.set("search", search);
    }

    if (status) {
      params = params.set("status", status);
    }

    return this.http.get<Page<Order>>(`${this.API_URL}/seller/me`, { params });
  }

  cancelOrder(orderId: string): Observable<Order> {
    return this.http.patch<Order>(`${this.API_URL}/${orderId}/cancel`, {});
  }

  redoOrder(orderId: string): Observable<Order> {
    return this.http.post<Order>(`${this.API_URL}/${orderId}/redo`, {});
  }

  deleteOrder(orderId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${orderId}`);
  }

  updateOrderStatus(
    orderId: string,
    request: UpdateOrderStatusRequest,
  ): Observable<Order> {
    return this.http.patch<Order>(`${this.API_URL}/${orderId}/status`, request);
  }
}
