import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";

@Component({
  selector: "app-theme-toggle",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./theme-toggle.component.html",
  styleUrls: ["./theme-toggle.component.scss"],
})
export class ThemeToggleComponent implements OnInit {
  isDark = false;
  private readonly THEME_KEY = "theme";

  ngOnInit(): void {
    const savedTheme = localStorage.getItem(this.THEME_KEY);
    if (savedTheme) {
      this.isDark = savedTheme === "dark";
    } else {
      this.isDark = globalThis.matchMedia(
        "(prefers-color-scheme: dark)",
      ).matches;
    }
    this.applyTheme();
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    localStorage.setItem(this.THEME_KEY, this.isDark ? "dark" : "light");
    this.applyTheme();
  }

  private applyTheme(): void {
    document.documentElement.dataset.theme = this.isDark ? "dark" : "light";
  }
}
