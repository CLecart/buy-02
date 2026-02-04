import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterOutlet, RouterLink } from "@angular/router";
import { AuthService } from "./services/auth.service";
import { User } from "./models/user.model";
import { ThemeToggleComponent } from "./components/theme-toggle/theme-toggle.component";

/**
 * AppComponent is the root component for the buy-02 frontend.
 *
 * Provides navigation, authentication state display, and router outlet.
 */
@Component({
  selector: "app-root",
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, ThemeToggleComponent],
  template: `
    <header class="navbar">
      <div class="nav-brand">
        <a routerLink="/">buy-02</a>
      </div>
      <nav class="nav-links">
        <a routerLink="/">Products</a>
        <a *ngIf="currentUser?.role === 'SELLER'" routerLink="/dashboard"
          >Dashboard</a
        >
      </nav>
      <div class="nav-auth">
        <ng-container *ngIf="currentUser; else authLinks">
          <span class="user-info">
            <img
              *ngIf="currentUser.avatarUrl"
              [src]="currentUser.avatarUrl"
              class="user-avatar"
              alt="Avatar"
            />
            {{ currentUser.name }}
          </span>
          <button class="btn-logout" (click)="logout()">Logout</button>
        </ng-container>
        <ng-template #authLinks>
          <a routerLink="/signin" class="nav-btn">Sign In</a>
          <a routerLink="/signup" class="nav-btn nav-btn-primary">Sign Up</a>
        </ng-template>
        <app-theme-toggle></app-theme-toggle>
      </div>
    </header>
    <main class="main-content">
      <router-outlet></router-outlet>
    </main>
  `,
  styleUrls: ["./app.component.scss"],
})
export class AppComponent implements OnInit {
  title = "buy-02";
  currentUser: User | null = null;

  constructor(private readonly authService: AuthService) {}

  ngOnInit(): void {
    this.authService.init();
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
