import {
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from "@angular/common/http";
import { Observable } from "rxjs";

const TOKEN_KEY = "auth_token";

/**
 * Functional HTTP interceptor that adds the JWT token to outgoing requests.
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const token = localStorage.getItem(TOKEN_KEY);

  if (token) {
    const authReq = req.clone({
      headers: req.headers.set("Authorization", `Bearer ${token}`),
    });
    return next(authReq);
  }

  return next(req);
};
