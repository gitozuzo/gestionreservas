import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { throwError } from 'rxjs/internal/observable/throwError';
import { catchError } from 'rxjs/internal/operators/catchError';
import { tap } from 'rxjs/internal/operators/tap';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http
      .post<{ token: string }>(`${this.API}/login`, { email, password })
      .pipe(
        tap((response) => {
          // Aquí puedes devolver el token al componente para que lo procese
          console.log('Login exitoso. Token recibido:', response.token);
        }),
        catchError((error) => {
          console.error('Error en login:', error);
          return throwError(
            () =>
              new Error('Credenciales incorrectas o servidor no disponible.')
          );
        })
      );
  }

  logout() {
    // this.authStore.logout();
    console.log('prueeba');
  }
}
