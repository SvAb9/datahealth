import { Routes } from '@angular/router';
import { rolGuard } from '../../core/guards/auth.guards';

export const HISTORIA_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./linea-tiempo').then((m) => m.LineaTiempo) },
  {
    path: ':idPaciente/nueva-atencion',
    canActivate: [rolGuard],
    data: { roles: ['PERSONAL_MEDICO'] },
    loadComponent: () => import('./registrar-atencion').then((m) => m.RegistrarAtencion),
  },
  { path: ':idPaciente', loadComponent: () => import('./linea-tiempo').then((m) => m.LineaTiempo) },
];