/**
 * Main entry point for buy-02 Angular frontend.
 */
import { bootstrapApplication } from "@angular/platform-browser";
import { AppComponent } from "./app/app.component";
import { appConfig } from "./app/app.config";

try {
  await bootstrapApplication(AppComponent, appConfig);
} catch (err: unknown) {
  console.error(err);
}
