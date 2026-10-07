import { Routes } from '@angular/router';

export const PACIENTES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./identificar-paciente').then((m) => m.IdentificarPaciente) },
];
