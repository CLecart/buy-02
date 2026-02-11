/**
 * Main entry point for buy-02 Angular frontend.
 */
import { bootstrapApplication } from "@angular/platform-browser";
import { AppComponent } from "./app/app.component";
import { appConfig } from "./app/app.config";

// Sonar S7785: prefer top-level await — apply it here for clarity and audit rules
try {
  await bootstrapApplication(AppComponent, appConfig);
} catch (err: unknown) {
  console.error(err);
}
