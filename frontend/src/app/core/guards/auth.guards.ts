import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/rol';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estaAutenticado() ? true : inject(Router).createUrlTree(['/login']);
};

/** Evita mostrar el login a quien ya inició sesión. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estaAutenticado() ? inject(Router).createUrlTree(['/inicio']) : true;
};

/** Uso: canActivate: [rolGuard], data: { roles: ['ADMIN_ENTIDAD'] } (RF-04). */
export const rolGuard: CanActivateFn = (route) => {
  const roles = (route.data['roles'] ?? []) as Rol[];
  return inject(AuthService).tieneRol(...roles) ? true : inject(Router).createUrlTree(['/inicio']);
};
