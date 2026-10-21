import { Component } from '@angular/core';
import { Routes } from '@angular/router';

@Component({
  standalone: true,
  template: '',
})
class AuditoriaComponent {}

export const AUDITORIA_ROUTES: Routes = [
  { path: '', loadComponent: () => Promise.resolve(AuditoriaComponent) },
];