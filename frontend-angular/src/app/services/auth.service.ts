import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable, tap } from "rxjs";
import {
  User,
  SignupRequest,
  SigninRequest,
  AuthResponse,
} from "../models/user.model";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private readonly API_URL = "/api";
  private readonly TOKEN_KEY = "auth_token";

  private readonly currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private initialized = false;

  constructor(private readonly http: HttpClient) {}

  init(): void {
    if (!this.initialized && this.getToken()) {
      this.initialized = true;
      this.getMe().subscribe({
        error: () => this.logout(),
      });
    }
  }

  signup(request: SignupRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/auth/signup`, request)
      .pipe(tap((response: AuthResponse) => this.setToken(response.token)));
  }

  signin(request: SigninRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/auth/signin`, request)
      .pipe(tap((response: AuthResponse) => this.setToken(response.token)));
  }

  getMe(): Observable<User> {
    return this.http
      .get<User>(`${this.API_URL}/users/me`)
      .pipe(tap((user: User) => this.currentUserSubject.next(user)));
  }

  uploadAvatar(file: File): Observable<User> {
    const formData = new FormData();
    formData.append("file", file);
    return this.http
      .post<User>(`${this.API_URL}/users/me/avatar`, formData)
      .pipe(tap((user: User) => this.currentUserSubject.next(user)));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isSeller(): boolean {
    return this.currentUserSubject.value?.role === "SELLER";
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this.getMe().subscribe();
  }
}
