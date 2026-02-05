import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ProfileService } from "../../services/profile.service";
import { AuthService } from "../../services/auth.service";
import { UserProfile, SellerProfile } from "../../models/profile.model";
import { User } from "../../models/user.model";

@Component({
  selector: "app-profile",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./profile.component.html",
  styleUrls: ["./profile.component.scss"],
})
export class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  userProfile: UserProfile | null = null;
  sellerProfile: SellerProfile | null = null;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (user) {
        this.loadProfiles(user);
      }
    });
  }

  private loadProfiles(user: User): void {
    this.profileService.getUserProfile(user.id).subscribe({
      next: (profile) => (this.userProfile = profile),
      error: (err: unknown) => console.error("Failed to load user profile", err),
    });

    if (user.role === "SELLER") {
      this.profileService.getSellerProfile(user.id).subscribe({
        next: (profile) => (this.sellerProfile = profile),
        error: (err: unknown) =>
          console.error("Failed to load seller profile", err),
      });
    }
  }
}
