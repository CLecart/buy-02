/**
 * Main entry point for buy-02 Angular frontend.
 */
import { bootstrapApplication } from "@angular/platform-browser";
import { AppComponent } from "./app/app.component";
import { appConfig } from "./app/app.config";

// NOSONAR: Top-level await not supported in Angular 17 production builds
// eslint-disable-next-line @typescript-eslint/no-floating-promises
bootstrapApplication(AppComponent, appConfig).catch((err: unknown) => {
  console.error(err);
});
