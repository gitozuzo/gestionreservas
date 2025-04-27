import { Injectable } from '@angular/core';

const TOKEN_KEY = 'token';
const ROLE_KEY = 'rol';
const NAME_KEY = 'name';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  constructor() {}

  // ✅ Verifica si hay token
  isAuthenticatedValue(): boolean {
    return !!localStorage.getItem(TOKEN_KEY);
  }

  // ✅ Obtiene el rol actual
  getRoleValue(): string | null {
    return localStorage.getItem(ROLE_KEY);
  }

  // ✅ Obtiene el nombre actual
  getNameValue(): string | null {
    return localStorage.getItem(NAME_KEY);
  }

  // ✅ Guarda los datos del token
  login(token: string): void {
    try {
      const base64 = token.split('.')[1];
      const payload = JSON.parse(decodeURIComponent(escape(atob(base64))));
      const rol = payload.rol || 'Empleado';
      const name = payload.sub;

      console.log('name', name);

      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(ROLE_KEY, rol);
      localStorage.setItem(NAME_KEY, name);
    } catch (err) {
      console.error('Token inválido', err);
      this.logout();
    }
  }

  // ✅ Limpia el estado y localStorage
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(NAME_KEY);
  }
}
