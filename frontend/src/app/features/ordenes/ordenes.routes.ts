import { Routes } from '@angular/router';

export const ORDENES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./ordenes').then((m) => m.Ordenes) },
];
