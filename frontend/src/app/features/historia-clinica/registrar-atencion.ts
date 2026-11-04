import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { mensajeDeError } from '../../core/http/api-error';
import { HistoriaClinicaService } from './historia-clinica.service';

/** HU-05: el médico registra la información clínica de una atención. */
@Component({
  selector: 'app-registrar-atencion',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="pagina">
      <a [routerLink]="['/historia-clinica', idPaciente()]">← Volver a la historia clínica</a>
      <h1>Registrar atención</h1>
      <p class="pagina__intro">Escribe la información clínica de esta consulta. Quedará en la historia clínica del paciente.</p>

      @if (error()) {
        <div class="aviso aviso--error" role="alert"><strong>No se registró la atención</strong> {{ error() }}</div>
      }

      <form class="panel" [formGroup]="form" (ngSubmit)="guardar()" novalidate>
        <div class="campo" [class.invalido]="invalido('diagnostico')">
          <label for="diagnostico">Diagnóstico</label>
          <textarea id="diagnostico" rows="3" formControlName="diagnostico"
                    [attr.aria-invalid]="invalido('diagnostico')"></textarea>
          @if (invalido('diagnostico')) { <p class="error">Escribe el diagnóstico (máximo 2000 caracteres).</p> }
        </div>

        <div class="campo" [class.invalido]="invalido('observaciones')">
          <label for="observaciones">Observaciones (opcional)</label>
          <textarea id="observaciones" rows="5" formControlName="observaciones"></textarea>
          @if (invalido('observaciones')) { <p class="error">Máximo 2000 caracteres.</p> }
        </div>

        <button class="btn" type="submit" [disabled]="guardando()">
          {{ guardando() ? 'Guardando…' : 'Guardar atención' }}
        </button>
      </form>
    </div>
  `,
})
export class RegistrarAtencion {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(HistoriaClinicaService);
  private readonly router = inject(Router);

  /** Viene de la ruta :idPaciente (withComponentInputBinding). */
  readonly idPaciente = input.required<string>();

  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    diagnostico: ['', [Validators.required, Validators.maxLength(2000)]],
    observaciones: ['', Validators.maxLength(2000)],
  });

  protected invalido(c: 'diagnostico' | 'observaciones'): boolean {
    const ctl = this.form.controls[c];
    return ctl.invalid && (ctl.touched || ctl.dirty);
  }

  protected guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.guardando.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();
    this.servicio.registrarAtencion(Number(this.idPaciente()), {
      diagnostico: v.diagnostico.trim(),
      observaciones: v.observaciones.trim() || undefined,
    }).subscribe({
      next: () => this.router.navigate(['/historia-clinica', this.idPaciente()]),
      error: (err) => { this.guardando.set(false); this.error.set(mensajeDeError(err)); },
    });
  }
}