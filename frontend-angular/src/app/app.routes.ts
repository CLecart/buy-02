import { Routes } from "@angular/router";
import { SigninComponent } from "./pages/signin/signin.component";
import { SignupComponent } from "./pages/signup/signup.component";
import { ProductListComponent } from "./pages/product-list/product-list.component";
import { SellerDashboardComponent } from "./pages/seller-dashboard/seller-dashboard.component";

export const routes: Routes = [
  { path: "", component: ProductListComponent },
  { path: "signin", component: SigninComponent },
  { path: "signup", component: SignupComponent },
  { path: "dashboard", component: SellerDashboardComponent },
  { path: "**", redirectTo: "" },
];
