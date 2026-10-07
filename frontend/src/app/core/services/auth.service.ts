import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Sesion } from '../models/auth.models';
import { Rol } from '../models/rol';

const CLAVE_TOKEN = 'dh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  // ADR-04: el token se guarda en sessionStorage (se pierde al cerrar la pestaña).
  private readonly _token = signal<string | null>(this.leerToken());

  readonly token = this._token.asReadonly();
  readonly sesion = computed<Sesion | null>(() => {
    const t = this._token();
    return t ? this.decodificar(t) : null;
  });

  login(credenciales: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, credenciales)
      .pipe(tap((r) => this.guardarToken(r.token)));
  }

  /** ADR-04: el logout solo descarta el token en el cliente. */
  logout(): void {
    this.borrarToken();
    this.router.navigate(['/login']);
  }

  estaAutenticado(): boolean {
    const s = this.sesion();
    return !!s && s.expiraEn > Date.now();
  }

  tieneRol(...roles: Rol[]): boolean {
    const s = this.sesion();
    return !!s && roles.includes(s.rol);
  }

  private guardarToken(token: string): void {
    try { sessionStorage.setItem(CLAVE_TOKEN, token); } catch { /* almacenamiento no disponible */ }
    this._token.set(token);
  }

  private borrarToken(): void {
    try { sessionStorage.removeItem(CLAVE_TOKEN); } catch { /* noop */ }
    this._token.set(null);
  }

  private leerToken(): string | null {
    try { return sessionStorage.getItem(CLAVE_TOKEN); } catch { return null; }
  }

  private decodificar(token: string): Sesion | null {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(base64).split('').map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''),
      );
      const c = JSON.parse(json);
      return {
        email: c.sub,
        nombre: c.nombre ?? c.sub,
        rol: c.rol,
        idEps: Number(c.id_eps ?? c.idEps),
        expiraEn: Number(c.exp) * 1000,
      };
    } catch {
      return null;
    }
  }
}
