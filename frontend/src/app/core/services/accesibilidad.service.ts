import { Injectable, signal } from '@angular/core';

const CLAVE = 'dh_accesibilidad';
export const ESCALAS = [1, 1.125, 1.25, 1.5] as const;

/** RNF-08: tamaño de fuente ajustable y contraste adecuado para adultos mayores. */
@Injectable({ providedIn: 'root' })
export class AccesibilidadService {
  readonly indiceEscala = signal(0);
  readonly altoContraste = signal(false);

  constructor() {
    try {
      const g = JSON.parse(localStorage.getItem(CLAVE) ?? '{}');
      if (typeof g.indice === 'number' && g.indice >= 0 && g.indice < ESCALAS.length) this.indiceEscala.set(g.indice);
      if (g.contraste === true) this.altoContraste.set(true);
    } catch { /* valores por defecto */ }
    this.aplicar();
  }

  aumentar(): void { this.cambiar(Math.min(this.indiceEscala() + 1, ESCALAS.length - 1)); }
  disminuir(): void { this.cambiar(Math.max(this.indiceEscala() - 1, 0)); }
  alternarContraste(): void { this.altoContraste.update((v) => !v); this.aplicar(); this.guardar(); }

  private cambiar(i: number): void { this.indiceEscala.set(i); this.aplicar(); this.guardar(); }

  private aplicar(): void {
    const raiz = document.documentElement;
    raiz.style.setProperty('--escala-texto', String(ESCALAS[this.indiceEscala()]));
    raiz.dataset['contraste'] = this.altoContraste() ? 'alto' : 'normal';
  }

  private guardar(): void {
    try { localStorage.setItem(CLAVE, JSON.stringify({ indice: this.indiceEscala(), contraste: this.altoContraste() })); } catch { /* noop */ }
  }
}
