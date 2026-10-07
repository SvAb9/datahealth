import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { mensajeDeError } from '../../core/http/api-error';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  template: `
    <div class="login">
      <section class="login__marca">
        <h1>Data-Health</h1>
        <p class="lema">Toda la historia clínica del paciente, en un solo lugar.</p>
        <p>Cada entidad de salud ve únicamente la información de sus propios pacientes.</p>
      </section>

      <section class="login__form">
        <h2>Iniciar sesión</h2>

        @if (error()) {
          <div class="aviso aviso--error" role="alert">
            <strong>No pudimos iniciar tu sesión</strong>
            {{ error() }}
          </div>
        }

        <form [formGroup]="form" (ngSubmit)="entrar()" novalidate>
          <div class="campo" [class.invalido]="invalido('email')">
            <label for="email">Correo electrónico</label>
            <input id="email" type="email" formControlName="email" autocomplete="username"
                   [attr.aria-invalid]="invalido('email')" aria-describedby="email-error">
            @if (invalido('email')) {
              <p class="error" id="email-error">Escribe un correo válido, por ejemplo nombre&#64;entidad.com.</p>
            }
          </div>

          <div class="campo" [class.invalido]="invalido('password')">
            <label for="password">Contraseña</label>
            <input id="password" [type]="verClave() ? 'text' : 'password'" formControlName="password"
                   autocomplete="current-password" [attr.aria-invalid]="invalido('password')"
                   aria-describedby="password-error">
            @if (invalido('password')) {
              <p class="error" id="password-error">Escribe tu contraseña.</p>
            }
            <p class="ayuda">
              <label class="inline"><input type="checkbox" [checked]="verClave()" (change)="verClave.set(!verClave())">
                Mostrar contraseña</label>
            </p>
          </div>

          <button class="btn btn--bloque" type="submit" [disabled]="enviando()">
            {{ enviando() ? 'Ingresando…' : 'Iniciar sesión' }}
          </button>
        </form>
      </section>
    </div>
  `,
  styles: `
    .login { min-height: 100vh; display: grid; grid-template-columns: minmax(0, 5fr) minmax(0, 6fr); }
    .login__marca { background: var(--puerto-oscuro); color: #fff; padding: 4rem 3.5rem; display: flex; flex-direction: column; justify-content: center; }
    .login__marca h1 { font-size: 2.6rem; margin-bottom: 1.5rem; }
    .lema { font-size: 1.6rem; line-height: 1.3; max-width: 22ch; }
    .login__form { padding: 3rem 2rem; display: flex; flex-direction: column; justify-content: center; max-width: 30rem; width: 100%; margin: 0 auto; }
    label.inline { display: inline-flex; align-items: center; gap: 0.6rem; font-weight: 400; cursor: pointer; }
    label.inline input { width: 1.4rem; min-height: 1.4rem; height: 1.4rem; }
    @media (max-width: 52rem) {
      .login { grid-template-columns: 1fr; }
      .login__marca { padding: 2rem 1.5rem; }
      .login__marca h1 { font-size: 1.8rem; margin-bottom: 0.5rem; }
      .lema { font-size: 1.2rem; }
    }
  `,
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verClave = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected invalido(campo: 'email' | 'password'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  protected entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/inicio']),
      error: (err) => {
        this.enviando.set(false);
        // El backend responde un mensaje genérico (ADR-04) y también informa el bloqueo temporal.
        this.error.set(mensajeDeError(err, 'Revisa tu correo y contraseña e intenta de nuevo.'));
      },
    });
  }
}
