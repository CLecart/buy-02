import { Routes } from "@angular/router";
import { SigninComponent } from "./pages/signin/signin.component";
import { SignupComponent } from "./pages/signup/signup.component";
import { ProductListComponent } from "./pages/product-list/product-list.component";
import { SellerDashboardComponent } from "./pages/seller-dashboard/seller-dashboard.component";
import { CartComponent } from "./pages/cart/cart.component";
import { OrdersComponent } from "./pages/orders/orders.component";
import { ProfileComponent } from "./pages/profile/profile.component";
import { WishlistComponent } from "./pages/wishlist/wishlist.component";

  { path: "", component: ProductListComponent },
  { path: "signin", component: SigninComponent },
  { path: "signup", component: SignupComponent },
  { path: "dashboard", component: SellerDashboardComponent },
  { path: "cart", component: CartComponent },
  { path: "orders", component: OrdersComponent },
  { path: "profile", component: ProfileComponent },
  { path: "wishlist", component: WishlistComponent },
  { path: "**", redirectTo: "" },
];
