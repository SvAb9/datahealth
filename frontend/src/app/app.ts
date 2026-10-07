import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AccesibilidadService } from './core/services/accesibilidad.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `<router-outlet />`,
})
export class App {
  // Se inyecta para aplicar de inmediato el tamaño de texto y contraste guardados.
  private readonly _accesibilidad = inject(AccesibilidadService);
}
