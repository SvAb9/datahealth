import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeDeError } from '../../core/http/api-error';
import { Paciente } from './pacientes.models';
import { PacientesService } from './pacientes.service';

type Estado = 'inicio' | 'buscando' | 'encontrado' | 'nuevo';

/** Proceso 1: identificar al paciente por documento o registrarlo si no existe. */
@Component({
  selector: 'app-identificar-paciente',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="pagina">
      <h1>Pacientes</h1>
      <p class="pagina__intro">Escribe el documento de identidad para ver si el paciente ya está registrado.</p>

      @if (error()) {
        <div class="aviso aviso--error" role="alert"><strong>Algo salió mal</strong> {{ error() }}</div>
      }

      <form class="panel busqueda" [formGroup]="busqueda" (ngSubmit)="buscar()" novalidate>
        <div class="campo" [class.invalido]="busqueda.controls.documento.invalid && busqueda.controls.documento.touched">
          <label for="documento">Documento de identidad</label>
          <input id="documento" inputmode="numeric" formControlName="documento" autocomplete="off">
          @if (busqueda.controls.documento.invalid && busqueda.controls.documento.touched) {
            <p class="error">Escribe solo números, entre 5 y 15 dígitos.</p>
          }
        </div>
        <button class="btn" type="submit" [disabled]="estado() === 'buscando'">
          {{ estado() === 'buscando' ? 'Buscando…' : 'Buscar paciente' }}
        </button>
      </form>

      @if (estado() === 'encontrado' && paciente(); as p) {
        <section class="panel" aria-live="polite">
          <div class="aviso aviso--ok"><strong>Paciente registrado</strong> Ya tiene expediente en el sistema.</div>
          <h2>{{ p.nombre }}</h2>
          <dl class="datos">
            <div><dt>Documento</dt><dd>{{ p.documento }}</dd></div>
            <div><dt>Edad</dt><dd>{{ p.edad }} años</dd></div>
            <div><dt>Teléfono</dt><dd>{{ p.telefono }}</dd></div>
            @if (p.direccion) { <div><dt>Dirección</dt><dd>{{ p.direccion }}</dd></div> }
            @if (p.eps) { <div><dt>Entidad de salud</dt><dd>{{ p.eps }}</dd></div> }
          </dl>
          <a class="btn btn--secundario" [routerLink]="['/historia-clinica', p.idPaciente]">Ver historia clínica</a>
        </section>
      }

      @if (estado() === 'nuevo') {
        <section class="panel" aria-live="polite">
          <div class="aviso aviso--info"><strong>No hay un paciente con ese documento</strong> Completa los datos para registrarlo. Se asociará a tu entidad de salud automáticamente.</div>

          <form [formGroup]="registro" (ngSubmit)="registrar()" novalidate>
            <div class="campo" [class.invalido]="inv('nombre')">
              <label for="nombre">Nombre completo</label>
              <input id="nombre" formControlName="nombre" autocomplete="off">
              @if (inv('nombre')) { <p class="error">Escribe el nombre completo.</p> }
            </div>
            <div class="campos-fila">
              <div class="campo" [class.invalido]="inv('edad')">
                <label for="edad">Edad</label>
                <input id="edad" type="number" inputmode="numeric" formControlName="edad">
                @if (inv('edad')) { <p class="error">Escribe una edad entre 0 y 120.</p> }
              </div>
              <div class="campo" [class.invalido]="inv('telefono')">
                <label for="telefono">Teléfono</label>
                <input id="telefono" type="tel" inputmode="tel" formControlName="telefono" autocomplete="off">
                @if (inv('telefono')) { <p class="error">Escribe un teléfono de contacto.</p> }
              </div>
            </div>
            <div class="campo">
              <label for="direccion">Dirección (opcional)</label>
              <input id="direccion" formControlName="direccion" autocomplete="off">
            </div>
            <button class="btn" type="submit" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : 'Registrar paciente' }}</button>
          </form>
        </section>
      }
    </div>
  `,
  styles: `
    .busqueda { display: flex; flex-wrap: wrap; align-items: flex-end; gap: 1rem; }
    .busqueda .campo { flex: 1 1 16rem; margin-bottom: 0; }
    .datos { display: grid; grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr)); gap: 1rem 2rem; margin: 1rem 0 1.5rem; }
    .datos dt { color: var(--tinta-suave); font-size: 0.9rem; }
    .datos dd { margin: 0; font-weight: 700; font-size: 1.1rem; }
  `,
})
export class IdentificarPaciente {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(PacientesService);

  protected readonly estado = signal<Estado>('inicio');
  protected readonly paciente = signal<Paciente | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly guardando = signal(false);

  protected readonly busqueda = this.fb.nonNullable.group({
    documento: ['', [Validators.required, Validators.pattern(/^\d{5,15}$/)]],
  });

  protected readonly registro = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    edad: [null as number | null, [Validators.required, Validators.min(0), Validators.max(120)]],
    telefono: ['', [Validators.required, Validators.minLength(7)]],
    direccion: [''],
  });

  protected inv(c: 'nombre' | 'edad' | 'telefono'): boolean {
    const ctl = this.registro.controls[c];
    return ctl.invalid && (ctl.touched || ctl.dirty);
  }

  protected buscar(): void {
    if (this.busqueda.invalid) { this.busqueda.markAllAsTouched(); return; }
    this.estado.set('buscando');
    this.error.set(null);
    this.paciente.set(null);
    this.servicio.buscarPorDocumento(this.busqueda.getRawValue().documento).subscribe({
      next: (p) => { this.paciente.set(p); this.estado.set('encontrado'); },
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) { this.registro.reset(); this.estado.set('nuevo'); }
        else { this.estado.set('inicio'); this.error.set(mensajeDeError(err)); }
      },
    });
  }

  protected registrar(): void {
    if (this.registro.invalid) { this.registro.markAllAsTouched(); return; }
    this.guardando.set(true);
    this.error.set(null);
    const v = this.registro.getRawValue();
    this.servicio.registrar({
      documento: this.busqueda.getRawValue().documento,
      nombre: v.nombre,
      edad: v.edad as number,
      telefono: v.telefono,
      direccion: v.direccion || undefined,
    }).subscribe({
      next: (p) => { this.guardando.set(false); this.paciente.set(p); this.estado.set('encontrado'); },
      error: (err) => { this.guardando.set(false); this.error.set(mensajeDeError(err)); },
    });
  }
}
