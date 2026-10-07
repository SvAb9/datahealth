import { Routes } from '@angular/router';

export const USUARIOS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./registrar-usuario').then((m) => m.RegistrarUsuario) },
];
