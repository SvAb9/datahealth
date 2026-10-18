import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { mensajeDeError } from '../../core/http/api-error';
import { ETIQUETA_ROL, Rol } from '../../core/models/rol';
import { UsuariosService } from './usuarios.service';

/** Roles que el administrador de entidad puede crear. */
const ROLES_CREABLES: Rol[] = ['PERSONAL_MEDICO', 'PERSONAL_ADMINISTRATIVO', 'PACIENTE'];

@Component({
  selector: 'app-registrar-usuario',
  imports: [ReactiveFormsModule],
  template: `
    <div class="pagina">
      <h1>Usuarios</h1>
      <p class="pagina__intro">Registra a las personas que usarán el sistema. Quedarán asociadas a tu entidad de salud.</p>

      @if (creado(); as nombre) {
        <div class="aviso aviso--ok" role="status"><strong>Usuario registrado</strong> {{ nombre }} ya puede iniciar sesión.</div>
      }
      @if (error()) {
        <div class="aviso aviso--error" role="alert"><strong>No se registró el usuario</strong> {{ error() }}</div>
      }

      <form class="panel" [formGroup]="form" (ngSubmit)="guardar()" novalidate>
        <div class="campo" [class.invalido]="invalido('nombre')">
          <label for="nombre">Nombre completo</label>
          <input id="nombre" formControlName="nombre" autocomplete="off" [attr.aria-invalid]="invalido('nombre')">
          @if (invalido('nombre')) { <p class="error">Escribe el nombre completo.</p> }
        </div>

        <div class="campos-fila">
          <div class="campo" [class.invalido]="invalido('email')">
            <label for="email">Correo electrónico</label>
            <input id="email" type="email" formControlName="email" autocomplete="off" [attr.aria-invalid]="invalido('email')">
            @if (invalido('email')) { <p class="error">Escribe un correo válido.</p> }
          </div>

          <div class="campo" [class.invalido]="invalido('rol')">
            <label for="rol">Rol</label>
            <select id="rol" formControlName="rol" [attr.aria-invalid]="invalido('rol')">
              <option value="" disabled>Elige un rol</option>
              @for (r of roles; track r) { <option [value]="r">{{ etiqueta[r] }}</option> }
            </select>
            @if (invalido('rol')) { <p class="error">Elige un rol.</p> }
          </div>
        </div>

        <div class="campo" [class.invalido]="invalido('password')">
          <label for="password">Contraseña inicial</label>
          <input id="password" type="password" formControlName="password" autocomplete="new-password" [attr.aria-invalid]="invalido('password')">
          @if (invalido('password')) {
            <p class="error">Usa al menos 8 caracteres.</p>
          } @else {
            <p class="ayuda">Mínimo 8 caracteres. La persona podrá usarla para su primer ingreso.</p>
          }
        </div>

        <button class="btn" type="submit" [disabled]="enviando()">{{ enviando() ? 'Guardando…' : 'Registrar usuario' }}</button>
      </form>
    </div>
  `,
})
export class RegistrarUsuario {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(UsuariosService);

  protected readonly roles = ROLES_CREABLES;
  protected readonly etiqueta = ETIQUETA_ROL;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly creado = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    rol: ['' as Rol | '', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  protected invalido(c: 'nombre' | 'email' | 'rol' | 'password'): boolean {
    const ctl = this.form.controls[c];
    return ctl.invalid && (ctl.touched || ctl.dirty);
  }

  protected guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.enviando.set(true);
    this.error.set(null);
    this.creado.set(null);
    const v = this.form.getRawValue();
    this.servicio.registrar({ ...v, rol: v.rol as Rol }).subscribe({
      next: (u) => {
        this.enviando.set(false);
        this.creado.set(u.nombre);
        this.form.reset({ nombre: '', email: '', rol: '', password: '' });
      },
      error: (err) => { this.enviando.set(false); this.error.set(mensajeDeError(err)); },
    });
  }
}
