import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink, Router } from "@angular/router";
import { SignupRequest } from "../../models/user.model";
import { AuthService } from "../../services/auth.service";
import { MediaService } from "../../services/media.service";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: "app-signup",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./signup.component.html",
  styleUrls: ["./signup.component.scss"],
})
export class SignupComponent {
  constructor(
    private readonly authService: AuthService,
    private readonly mediaService: MediaService,
    private readonly router: Router,
  ) {}
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

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      const error = this.mediaService.validateFile(file);
      if (error) {
        this.avatarError = error;
        this.selectedAvatar = null;
        if (this.avatarPreview) {
          URL.revokeObjectURL(this.avatarPreview);
        }
        this.avatarPreview = null;
        return;
      }
      this.avatarError = "";
      this.selectedAvatar = file;
      if (this.avatarPreview) {
        URL.revokeObjectURL(this.avatarPreview);
      }
      this.avatarPreview = null;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview = e?.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    this.loading = true;
    this.errorMessage = "";

    this.authService.signup(this.request).subscribe({
      next: () => {
        const loadUserAndNavigate = () => {
          this.authService.getMe().subscribe({
            next: () => {
              this.router.navigate(["/"]);
            },
            error: () => {
              this.router.navigate(["/"]);
            },
          });
        };

        if (this.request.role === "SELLER" && this.selectedAvatar) {
          this.authService.uploadAvatar(this.selectedAvatar).subscribe({
            next: loadUserAndNavigate,
            error: loadUserAndNavigate,
          });
        } else {
          loadUserAndNavigate();
        }
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        console.error("Signup error:", err);
        if (
          err.status === 409 ||
          (err.error?.message && /email/i.test(err.error.message))
        ) {
          this.errorMessage =
            err.error?.message ||
            "Registration failed. Email is already in use.";
        } else {
          // Prefer server-provided message when available for better debugging
          this.errorMessage =
            err.error?.message ||
            err.message ||
            "Registration failed. Please try again later.";
        }
      },
    });
  }
}
