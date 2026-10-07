import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MODULOS } from '../core/navigation/modulos';
import { ETIQUETA_ROL } from '../core/models/rol';
import { AccesibilidadService, ESCALAS } from '../core/services/accesibilidad.service';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <a class="saltar" href="#contenido">Saltar al contenido</a>

    <div class="marco">
      <header class="barra">
        <a routerLink="/inicio" class="marca">Data-Health</a>

        <div class="accesibilidad" role="group" aria-label="Opciones de lectura">
          <span class="accesibilidad__titulo">Tamaño del texto</span>
          <button type="button" class="ctl" (click)="acc.disminuir()"
                  [disabled]="acc.indiceEscala() === 0" aria-label="Reducir texto">A−</button>
          <button type="button" class="ctl" (click)="acc.aumentar()"
                  [disabled]="acc.indiceEscala() === maxEscala" aria-label="Aumentar texto">A+</button>
          <button type="button" class="ctl ctl--ancho" (click)="acc.alternarContraste()"
                  [attr.aria-pressed]="acc.altoContraste()">Contraste alto</button>
        </div>

        <div class="usuario">
          <div>
            <strong>{{ auth.sesion()?.nombre }}</strong>
            <span>{{ etiquetaRol() }}</span>
          </div>
          <button type="button" class="btn btn--secundario" (click)="auth.logout()">Cerrar sesión</button>
        </div>
      </header>

      <nav class="menu" aria-label="Módulos">
        <a routerLink="/inicio" routerLinkActive="activo">Inicio</a>
        @for (m of modulos(); track m.ruta) {
          <a [routerLink]="m.ruta" routerLinkActive="activo">{{ m.etiqueta }}</a>
        }
      </nav>

      <main id="contenido" class="contenido" tabindex="-1">
        <router-outlet />
      </main>
    </div>
  `,
  styles: `
    .saltar { position: absolute; left: -999px; background: var(--papel); padding: 0.5rem 1rem; z-index: 10; }
    .saltar:focus { left: 0.5rem; top: 0.5rem; }

    .marco { min-height: 100vh; display: grid; grid-template-columns: 15rem 1fr; grid-template-rows: auto 1fr;
             grid-template-areas: 'barra barra' 'menu contenido'; }

    .barra { grid-area: barra; display: flex; flex-wrap: wrap; align-items: center; gap: 1rem 2rem;
             padding: 0.75rem 1.5rem; background: var(--puerto-oscuro); color: #fff; }
    .marca { font-size: 1.4rem; font-weight: 700; color: #fff; text-decoration: none; margin-right: auto; }

    .accesibilidad { display: flex; align-items: center; gap: 0.5rem; }
    .accesibilidad__titulo { font-size: 0.9rem; margin-right: 0.25rem; }
    .ctl { min-height: 2.75rem; min-width: 2.75rem; padding: 0 0.75rem; font: inherit; font-weight: 700; cursor: pointer;
           color: #fff; background: transparent; border: 2px solid #fff; border-radius: var(--radio); }
    .ctl--ancho { padding: 0 1rem; }
    .ctl:hover:not(:disabled) { background: rgba(255,255,255,.18); }
    .ctl:disabled { opacity: .45; cursor: not-allowed; }
    .ctl[aria-pressed='true'] { background: #fff; color: var(--puerto-oscuro); }
    .barra :focus-visible { box-shadow: 0 0 0 0.1875rem var(--puerto-oscuro), 0 0 0 0.375rem #fff; }

    .usuario { display: flex; align-items: center; gap: 1rem; }
    .usuario div { display: flex; flex-direction: column; line-height: 1.25; text-align: right; }
    .usuario span { font-size: 0.9rem; opacity: .85; }
    .usuario .btn--secundario { color: #fff; border-color: #fff; min-height: 2.75rem; }
    .usuario .btn--secundario:hover:not(:disabled) { background: #fff; color: var(--puerto-oscuro); }

    .menu { grid-area: menu; display: flex; flex-direction: column; gap: 0.25rem; padding: 1.25rem 0.75rem;
            background: var(--papel); border-right: 2px solid var(--linea); }
    .menu a { padding: 0.8rem 1rem; font-weight: 700; text-decoration: none; color: var(--tinta); border-radius: var(--radio);
              border-left: 0.35rem solid transparent; }
    .menu a:hover { background: var(--niebla); }
    .menu a.activo { background: var(--niebla); border-left-color: var(--puerto); color: var(--puerto); }

    .contenido { grid-area: contenido; padding: 2rem 2.5rem; outline: none; }

    @media (max-width: 52rem) {
      .marco { grid-template-columns: 1fr; grid-template-areas: 'barra' 'menu' 'contenido'; }
      .menu { flex-direction: row; flex-wrap: wrap; border-right: 0; border-bottom: 2px solid var(--linea); padding: 0.5rem; }
      .contenido { padding: 1.25rem 1rem; }
      .usuario { width: 100%; justify-content: space-between; }
      .usuario div { text-align: left; }
    }
  `,
})
export class Shell {
  protected readonly auth = inject(AuthService);
  protected readonly acc = inject(AccesibilidadService);
  protected readonly maxEscala = ESCALAS.length - 1;

  protected readonly etiquetaRol = computed(() => {
    const s = this.auth.sesion();
    return s ? ETIQUETA_ROL[s.rol] : '';
  });

  /** Solo se muestran los módulos permitidos para el rol (RF-04). */
  protected readonly modulos = computed(() => {
    const s = this.auth.sesion();
    return s ? MODULOS.filter((m) => m.roles.includes(s.rol)) : [];
  });
}
