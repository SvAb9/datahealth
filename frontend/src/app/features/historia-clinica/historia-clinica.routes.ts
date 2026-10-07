import { Routes } from '@angular/router';

export const HISTORIA_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./linea-tiempo').then((m) => m.LineaTiempo) },
  { path: ':idPaciente', loadComponent: () => import('./linea-tiempo').then((m) => m.LineaTiempo) },
];
