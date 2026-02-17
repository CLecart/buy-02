import { bootstrapApplication } from "@angular/platform-browser";
import { AppComponent } from "./app/app.component";
import { appConfig } from "./app/app.config";
import { environment } from "./environments/environment";
export const __module = true;
/**
 * Main entry point for buy-02 Angular frontend.
 *
 * Notes:
 * - Uses top-level await in the bootstrap to comply with Sonar rule S7785.
 * - Build configuration (see `frontend-angular/tsconfig.json` and `.browserslistrc`) is set to support top-level await (target ES2022 + module ESNext, modern browsers).
 * - Rationale is recorded in the commit/PR description; avoid inline non-doc comments.
 */

try {
  await bootstrapApplication(AppComponent, appConfig);
  if (!environment.production) {
    console.log("API URL:", environment.apiUrl);
  }
} catch (err: unknown) {
  console.error(err);
}
