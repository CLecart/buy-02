import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { MediaService } from "../../services/media.service";
import { SignupRequest } from "../../models/user.model";

/**
 * Sign-up page component with avatar upload for sellers.
 */
@Component({
  selector: "app-signup",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./signup.component.html",
  styleUrls: ["./signup.component.scss"],
})
export class SignupComponent {
  request: SignupRequest = {
    name: "",
    email: "",
    password: "",
    role: "CLIENT",
  };
  selectedAvatar: File | null = null;
  avatarPreview: string | null = null;
  avatarError = "";
  loading = false;
  errorMessage = "";

  constructor(
    private authService: AuthService,
    private mediaService: MediaService,
    private router: Router
  ) {}

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validate file
      const error = this.mediaService.validateFile(file);
      if (error) {
        this.avatarError = error;
        this.selectedAvatar = null;
        this.avatarPreview = null;
        return;
      }

      this.avatarError = "";
      this.selectedAvatar = file;

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    this.loading = true;
    this.errorMessage = "";

    this.authService.signup(this.request).subscribe({
      next: () => {
        // Load user info after successful signup
        const loadUserAndNavigate = () => {
          this.authService.getMe().subscribe({
            next: () => this.router.navigate(["/"]),
            error: () => this.router.navigate(["/"]),
          });
        };

        // If seller with avatar, upload it first
        if (this.request.role === "SELLER" && this.selectedAvatar) {
          this.authService.uploadAvatar(this.selectedAvatar).subscribe({
            next: () => loadUserAndNavigate(),
            error: () => loadUserAndNavigate(), // Continue even if avatar fails
          });
        } else {
          loadUserAndNavigate();
        }
      },
      error: (err: { error?: { message?: string } }) => {
        this.loading = false;
        this.errorMessage =
          err.error?.message ||
          "Registration failed. Email may already be in use.";
      },
    });
  }
}
