import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MODULOS } from '../../core/navigation/modulos';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  template: `
    <div class="pagina">
      <h1>Hola, {{ nombre() }}</h1>
      <p class="pagina__intro">Estos son los módulos a los que puedes entrar.</p>

      <ul class="accesos">
        @for (m of modulos(); track m.ruta) {
          <li>
            <a [routerLink]="m.ruta">
              <span class="accesos__titulo">{{ m.etiqueta }}</span>
              <span class="accesos__texto">{{ m.descripcion }}</span>
            </a>
          </li>
        }
      </ul>
    </div>
  `,
  styles: `
    .accesos { list-style: none; margin: 0; padding: 0; border-top: 2px solid var(--linea); }
    .accesos li { border-bottom: 2px solid var(--linea); }
    .accesos a { display: block; padding: 1.25rem 0.75rem; text-decoration: none; color: var(--tinta); border-radius: 0; }
    .accesos a:hover { background: var(--papel); }
    .accesos__titulo { display: block; font-size: 1.3rem; font-weight: 700; color: var(--puerto); }
    .accesos__texto { color: var(--tinta-suave); }
  `,
})
export class Inicio {
  private readonly auth = inject(AuthService);
  protected readonly nombre = computed(() => this.auth.sesion()?.nombre.split(' ')[0] ?? '');
  protected readonly modulos = computed(() => {
    const s = this.auth.sesion();
    return s ? MODULOS.filter((m) => m.roles.includes(s.rol)) : [];
  });
}
