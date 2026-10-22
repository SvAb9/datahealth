import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { mensajeDeError } from '../../core/http/api-error';
import { Eps } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth.service';

type Campo = 'idEps' | 'documento' | 'nombre' | 'edad' | 'telefono' | 'email' | 'password';

/** Autorregistro: solo para pacientes. El resto de roles los crea el administrador de la entidad. */
@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="registro">
      <a routerLink="/login">← Volver a iniciar sesión</a>
      <h1>Crear cuenta de paciente</h1>
      <p class="pagina__intro">
        Este registro es solo para pacientes. Si trabajas en una entidad de salud, pide tu usuario
        al administrador de tu entidad.
      </p>

      @if (error()) {
        <div class="aviso aviso--error" role="alert"><strong>No pudimos crear tu cuenta</strong> {{ error() }}</div>
      }

      <form class="panel" [formGroup]="form" (ngSubmit)="enviar()" novalidate>
        <div class="campo" [class.invalido]="invalido('idEps')">
          <label for="idEps">Entidad de salud (EPS)</label>
          <select id="idEps" formControlName="idEps" [attr.aria-invalid]="invalido('idEps')">
            <option [ngValue]="null" disabled>{{ cargandoEntidades() ? 'Cargando entidades…' : 'Elige tu entidad' }}</option>
            @for (e of entidades(); track e.idEps) { <option [ngValue]="e.idEps">{{ e.nombre }}</option> }
          </select>
          @if (invalido('idEps')) { <p class="error">Elige tu entidad de salud.</p> }
        </div>

        <div class="campos-fila">
          <div class="campo" [class.invalido]="invalido('documento')">
            <label for="documento">Documento de identidad</label>
            <input id="documento" inputmode="numeric" formControlName="documento" autocomplete="off"
                   [attr.aria-invalid]="invalido('documento')">
            @if (invalido('documento')) { <p class="error">Escribe solo números, entre 5 y 15 dígitos.</p> }
          </div>
          <div class="campo" [class.invalido]="invalido('edad')">
            <label for="edad">Edad</label>
            <input id="edad" type="number" inputmode="numeric" formControlName="edad"
                   [attr.aria-invalid]="invalido('edad')">
            @if (invalido('edad')) { <p class="error">Escribe una edad entre 0 y 120.</p> }
          </div>
        </div>

        <div class="campo" [class.invalido]="invalido('nombre')">
          <label for="nombre">Nombre completo</label>
          <input id="nombre" formControlName="nombre" autocomplete="name" [attr.aria-invalid]="invalido('nombre')">
          @if (invalido('nombre')) { <p class="error">Escribe tu nombre completo.</p> }
        </div>

        <div class="campos-fila">
          <div class="campo" [class.invalido]="invalido('telefono')">
            <label for="telefono">Teléfono</label>
            <input id="telefono" type="tel" inputmode="tel" formControlName="telefono" autocomplete="tel"
                   [attr.aria-invalid]="invalido('telefono')">
            @if (invalido('telefono')) { <p class="error">Escribe un teléfono de contacto.</p> }
          </div>
          <div class="campo">
            <label for="direccion">Dirección (opcional)</label>
            <input id="direccion" formControlName="direccion" autocomplete="street-address">
          </div>
        </div>

        <div class="campo" [class.invalido]="invalido('email')">
          <label for="email">Correo electrónico</label>
          <input id="email" type="email" formControlName="email" autocomplete="email"
                 [attr.aria-invalid]="invalido('email')">
          @if (invalido('email')) { <p class="error">Escribe un correo válido.</p> }
        </div>

        <div class="campo" [class.invalido]="invalido('password')">
          <label for="password">Contraseña</label>
          <input id="password" [type]="verClave() ? 'text' : 'password'" formControlName="password"
                 autocomplete="new-password" [attr.aria-invalid]="invalido('password')">
          @if (invalido('password')) {
            <p class="error">Usa al menos 8 caracteres.</p>
          } @else {
            <p class="ayuda">Mínimo 8 caracteres.</p>
          }
          <p class="ayuda">
            <label class="inline"><input type="checkbox" [checked]="verClave()" (change)="verClave.set(!verClave())">
              Mostrar contraseña</label>
          </p>
        </div>

        <button class="btn btn--bloque" type="submit" [disabled]="enviando()">
          {{ enviando() ? 'Creando cuenta…' : 'Crear mi cuenta' }}
        </button>
      </form>
    </main>
  `,
  styles: `
    .registro { max-width: 40rem; margin: 0 auto; padding: 2rem 1rem 3rem; }
    label.inline { display: inline-flex; align-items: center; gap: 0.6rem; font-weight: 400; cursor: pointer; }
    label.inline input { width: 1.4rem; min-height: 1.4rem; height: 1.4rem; }
  `,
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly entidades = signal<Eps[]>([]);
  protected readonly cargandoEntidades = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verClave = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    idEps: [null as number | null, Validators.required],
    documento: ['', [Validators.required, Validators.pattern(/^\d{5,15}$/)]],
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    edad: [null as number | null, [Validators.required, Validators.min(0), Validators.max(120)]],
    telefono: ['', [Validators.required, Validators.minLength(7)]],
    direccion: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  constructor() {
    this.auth.listarEps().subscribe({
      next: (lista) => { this.entidades.set(lista); this.cargandoEntidades.set(false); },
      error: (err) => {
        this.cargandoEntidades.set(false);
        this.error.set(mensajeDeError(err, 'No pudimos cargar las entidades de salud. Intenta de nuevo.'));
      },
    });
  }

  protected invalido(campo: Campo): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();
    this.auth.registrarPaciente({
      idEps: v.idEps as number,
      documento: v.documento.trim(),
      nombre: v.nombre.trim(),
      edad: v.edad as number,
      telefono: v.telefono.trim(),
      direccion: v.direccion.trim() || undefined,
      email: v.email.trim(),
      password: v.password,
    }).subscribe({
      next: () => this.router.navigate(['/inicio']),
      error: (err) => { this.enviando.set(false); this.error.set(mensajeDeError(err)); },
    });
  }
}