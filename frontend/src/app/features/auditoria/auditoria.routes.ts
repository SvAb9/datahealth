import { Routes } from '@angular/router';

export const AUDITORIA_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./auditoria').then((m) => m.Auditoria) },
];
