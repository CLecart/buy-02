import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { SigninRequest } from "../../models/user.model";

/**
 * Sign-in page component.
 */
@Component({
  selector: "app-signin",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./signin.component.html",
  styleUrls: ["./signin.component.scss"],
})
export class SigninComponent {
  credentials: SigninRequest = { email: "", password: "" };
  loading = false;
  errorMessage = "";

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.loading = true;
    this.errorMessage = "";

    this.authService.signin(this.credentials).subscribe({
      next: () => {
        // Load user info after successful signin
        this.authService.getMe().subscribe({
          next: () => this.router.navigate(["/"]),
          error: () => this.router.navigate(["/"]),
        });
      },
      error: (err: { error?: { message?: string } }) => {
        this.loading = false;
        this.errorMessage = err.error?.message || "Invalid email or password";
      },
    });
  }
}
