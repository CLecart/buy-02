import { ComponentFixture, TestBed } from "@angular/core/testing";
import { CartComponent } from "./cart.component";
import { CartService } from "../../services/cart.service";
import { OrderService } from "../../services/order.service";
import { AuthService } from "../../services/auth.service";
import { of } from "rxjs";

// Mock services
const cartServiceMock = {
  getCart: () => of({ items: [], totalPrice: 0 }),
  addItem: () => of({}),
  updateItem: () => of({}),
  removeItem: () => of({}),
  clearCart: () => of({}),
  deleteCart: () => of({}),
};
const orderServiceMock = {};
const authServiceMock = {
  currentUser$: of({ id: "user1", email: "test@test.com", role: "CLIENT" }),
  isAuthenticated: () => true,
};

describe("CartComponent", () => {
  let component: CartComponent;
  let fixture: ComponentFixture<CartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartComponent],
      providers: [
        { provide: CartService, useValue: cartServiceMock },
        { provide: OrderService, useValue: orderServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(CartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should load cart on init", () => {
    expect(component.cart).toBeDefined();
  });

  it("should update quantity", () => {
    spyOn(cartServiceMock, "updateItem").and.callThrough();
    component.updateQuantity("prod1", 2);
    expect(cartServiceMock.updateItem).toHaveBeenCalled();
  });

  it("should remove item", () => {
    spyOn(cartServiceMock, "removeItem").and.callThrough();
    component.removeItem("prod1");
    expect(cartServiceMock.removeItem).toHaveBeenCalled();
  });

  it("should clear cart", () => {
    spyOn(cartServiceMock, "clearCart").and.callThrough();
    component.clearCart();
    expect(cartServiceMock.clearCart).toHaveBeenCalled();
  });
});
