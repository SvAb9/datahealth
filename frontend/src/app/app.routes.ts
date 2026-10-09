import { Routes } from '@angular/router';
import { authGuard, invitadoGuard, rolGuard } from './core/guards/auth.guards';

/** Cada ruta de módulo declara los roles permitidos (RF-04). */
export const routes: Routes = [
  {
    path: 'login',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      { path: 'inicio', loadComponent: () => import('./features/inicio/inicio').then((m) => m.Inicio) },
      {
        path: 'usuarios',
        canActivate: [rolGuard],
        data: { roles: ['ADMIN_ENTIDAD'] },
        loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
      },
      {
        path: 'pacientes',
        canActivate: [rolGuard],
        data: { roles: ['PERSONAL_MEDICO', 'PERSONAL_ADMINISTRATIVO'] },
        loadChildren: () => import('./features/pacientes/pacientes.routes').then((m) => m.PACIENTES_ROUTES),
      },
      {
        path: 'historia-clinica',
        canActivate: [rolGuard],
        data: { roles: ['PERSONAL_MEDICO', 'PACIENTE'] },
        loadChildren: () => import('./features/historia-clinica/historia-clinica.routes').then((m) => m.HISTORIA_ROUTES),
      },
      {
        path: 'ordenes',
        canActivate: [rolGuard],
        data: { roles: ['PERSONAL_MEDICO'] },
        loadChildren: () => import('./features/ordenes/ordenes.routes').then((m) => m.ORDENES_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
