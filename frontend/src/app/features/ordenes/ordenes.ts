import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeDeError } from '../../core/http/api-error';
import { Atencion, Orden, Resultado } from '../historia-clinica/historia-clinica.models';
import { HistoriaClinicaService } from '../historia-clinica/historia-clinica.service';
import { Paciente } from '../pacientes/pacientes.models';
import { PacientesService } from '../pacientes/pacientes.service';
import { OrdenesService } from './ordenes.service';

const ETIQUETA_ORDEN = { MEDICA: 'Orden médica', MEDICAMENTO: 'Medicamento', LABORATORIO: 'Laboratorio' } as const;

/** M4 · HU-07: el médico agrega órdenes a una atención abierta y carga resultados de laboratorio. */
@Component({
  selector: 'app-ordenes',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  template: `
    <div class="pagina">
      <h1>Consultas y órdenes</h1>
      <p class="pagina__intro">Busca al paciente por documento para agregar órdenes a sus atenciones y cargar resultados.</p>

      @if (error()) {
        <div class="aviso aviso--error" role="alert"><strong>Algo salió mal</strong> {{ error() }}</div>
      }
      @if (aviso()) {
        <div class="aviso aviso--ok" role="status"><strong>Listo</strong> {{ aviso() }}</div>
      }

      <form class="panel busqueda" [formGroup]="busqueda" (ngSubmit)="buscar()" novalidate>
        <div class="campo" [class.invalido]="busqueda.controls.documento.invalid && busqueda.controls.documento.touched">
          <label for="documento">Documento de identidad</label>
          <input id="documento" inputmode="numeric" formControlName="documento" autocomplete="off">
          @if (busqueda.controls.documento.invalid && busqueda.controls.documento.touched) {
            <p class="error">Escribe solo números, entre 5 y 15 dígitos.</p>
          }
        </div>
        <button class="btn" type="submit" [disabled]="cargando()">{{ cargando() ? 'Buscando…' : 'Buscar paciente' }}</button>
      </form>

      @if (paciente(); as p) {
        <section class="panel">
          <h2>{{ p.nombre }}</h2>
          <p>Documento {{ p.documento }} · {{ p.edad }} años</p>
          <a class="btn btn--secundario" [routerLink]="['/historia-clinica', p.idPaciente, 'nueva-atencion']">Registrar nueva atención</a>
        </section>

        @if (atenciones().length === 0) {
          <div class="vacio">
            <h2>Aún no hay atenciones</h2>
            <p>Registra una atención para poder agregarle órdenes.</p>
          </div>
        }

        @for (a of atenciones(); track a.idCita) {
          <section class="panel">
            <h2>{{ a.fecha | date: "d 'de' MMMM 'de' y" }}</h2>
            <p class="meta">
              <span class="estado" [class.estado--cerrada]="a.estado === 'CERRADA'">
                {{ a.estado === 'CERRADA' ? 'Atención cerrada' : 'Atención abierta' }}
              </span>
              Atendió: {{ a.medico }}
            </p>
            @if (a.diagnostico) { <p><strong>Diagnóstico:</strong> {{ a.diagnostico }}</p> }

            @if (a.ordenes.length) {
              <ul class="ordenes">
                @for (o of a.ordenes; track o.idOrden) {
                  <li>
                    <strong>{{ etiquetaOrden[o.tipo] }}:</strong> {{ o.descripcion }}
                    @if (o.tipo === 'LABORATORIO') {
                      @if (resultadoDe(a, o); as r) {
                        <div class="resultado"><strong>Resultado ({{ r.fechaResultado | date: 'dd/MM/y' }}):</strong> {{ r.resultado }}</div>
                      } @else if (ordenActiva() === o.idOrden) {
                        <form class="formulario-en-linea" [formGroup]="formResultado" (ngSubmit)="guardarResultado()" novalidate>
                          <div class="campo" [class.invalido]="formResultado.controls.resultado.invalid && formResultado.controls.resultado.touched">
                            <label [for]="'res-' + o.idOrden">Resultado del examen</label>
                            <textarea [id]="'res-' + o.idOrden" rows="3" formControlName="resultado"></textarea>
                            @if (formResultado.controls.resultado.invalid && formResultado.controls.resultado.touched) {
                              <p class="error">Escribe el resultado (máximo 2000 caracteres).</p>
                            }
                          </div>
                          <button class="btn" type="submit" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : 'Guardar resultado' }}</button>
                          <button class="btn btn--secundario" type="button" (click)="cancelar()">Cancelar</button>
                        </form>
                      } @else {
                        <div><button class="btn btn--secundario" type="button" (click)="abrirResultado(o.idOrden)">Registrar resultado</button></div>
                      }
                    }
                  </li>
                }
              </ul>
            } @else {
              <p>Esta atención aún no tiene órdenes.</p>
            }

            @if (a.estado === 'ABIERTA') {
              @if (citaActiva() === a.idCita) {
                <form class="formulario-en-linea" [formGroup]="formOrden" (ngSubmit)="guardarOrden()" novalidate>
                  <div class="campo" [class.invalido]="formOrden.controls.tipo.invalid && formOrden.controls.tipo.touched">
                    <label [for]="'tipo-' + a.idCita">Tipo de orden</label>
                    <select [id]="'tipo-' + a.idCita" formControlName="tipo">
                      <option value="" disabled>Elige un tipo</option>
                      @for (t of tipos; track t[0]) { <option [value]="t[0]">{{ t[1] }}</option> }
                    </select>
                    @if (formOrden.controls.tipo.invalid && formOrden.controls.tipo.touched) { <p class="error">Elige el tipo de orden.</p> }
                  </div>
                  <div class="campo" [class.invalido]="formOrden.controls.descripcion.invalid && formOrden.controls.descripcion.touched">
                    <label [for]="'desc-' + a.idCita">Descripción</label>
                    <textarea [id]="'desc-' + a.idCita" rows="3" formControlName="descripcion"></textarea>
                    @if (formOrden.controls.descripcion.invalid && formOrden.controls.descripcion.touched) {
                      <p class="error">Escribe la descripción (máximo 1000 caracteres).</p>
                    }
                  </div>
                  <button class="btn" type="submit" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : 'Guardar orden' }}</button>
                  <button class="btn btn--secundario" type="button" (click)="cancelar()">Cancelar</button>
                </form>
              } @else {
                <button class="btn" type="button" (click)="abrirOrden(a.idCita)">Agregar orden</button>
              }
            }
          </section>
        }
      }
    </div>
  `,
  styles: `
    .busqueda { display: flex; flex-wrap: wrap; align-items: flex-end; gap: 1rem; }
    .busqueda .campo { flex: 1 1 16rem; margin-bottom: 0; }
    .meta { color: var(--tinta-suave); }
    .estado { display: inline-block; margin-right: 0.75rem; padding: 0.1rem 0.7rem; font-weight: 700; border: 2px solid var(--tinta-suave); border-radius: 2rem; }
    .estado--cerrada { border-color: var(--musgo); color: var(--musgo); }
    .ordenes { margin: 0 0 1rem; padding-left: 1.25rem; }
    .ordenes li { margin-bottom: 0.75rem; }
    .resultado { background: var(--niebla); border-left: 0.4rem solid var(--musgo); padding: 0.5rem 0.8rem; margin-top: 0.4rem; }
    .formulario-en-linea { margin-top: 0.75rem; padding: 1rem; border: 2px solid var(--linea); border-radius: var(--radio); }
    .formulario-en-linea .btn { margin-right: 0.5rem; }
  `,
})
export class Ordenes {
  private readonly fb = inject(FormBuilder);
  private readonly pacientes = inject(PacientesService);
  private readonly historia = inject(HistoriaClinicaService);
  private readonly ordenes = inject(OrdenesService);

  protected readonly etiquetaOrden = ETIQUETA_ORDEN;
  protected readonly tipos = Object.entries(ETIQUETA_ORDEN);

  protected readonly paciente = signal<Paciente | null>(null);
  protected readonly atenciones = signal<Atencion[]>([]);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly citaActiva = signal<number | null>(null);
  protected readonly ordenActiva = signal<number | null>(null);

  protected readonly busqueda = this.fb.nonNullable.group({
    documento: ['', [Validators.required, Validators.pattern(/^\d{5,15}$/)]],
  });

  protected readonly formOrden = this.fb.nonNullable.group({
    tipo: ['' as Orden['tipo'] | '', Validators.required],
    descripcion: ['', [Validators.required, Validators.maxLength(1000)]],
  });

  protected readonly formResultado = this.fb.nonNullable.group({
    resultado: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  protected resultadoDe(a: Atencion, o: Orden): Resultado | undefined {
    return a.resultados.find((r) => r.idOrden === o.idOrden);
  }

  protected abrirOrden(idCita: number): void {
    this.cancelar();
    this.formOrden.reset({ tipo: '', descripcion: '' });
    this.citaActiva.set(idCita);
  }

  protected abrirResultado(idOrden: number): void {
    this.cancelar();
    this.formResultado.reset({ resultado: '' });
    this.ordenActiva.set(idOrden);
  }

  protected cancelar(): void {
    this.citaActiva.set(null);
    this.ordenActiva.set(null);
    this.error.set(null);
  }

  protected buscar(): void {
    if (this.busqueda.invalid) { this.busqueda.markAllAsTouched(); return; }
    this.cargando.set(true);
    this.error.set(null);
    this.aviso.set(null);
    this.paciente.set(null);
    this.atenciones.set([]);
    this.cancelar();
    this.pacientes.buscarPorDocumento(this.busqueda.getRawValue().documento).subscribe({
      next: (p) => { this.paciente.set(p); this.recargar(p.idPaciente); },
      error: (err) => { this.cargando.set(false); this.error.set(mensajeDeError(err)); },
    });
  }

  protected guardarOrden(): void {
    const idCita = this.citaActiva();
    if (idCita === null) return;
    if (this.formOrden.invalid) { this.formOrden.markAllAsTouched(); return; }
    this.guardando.set(true);
    this.error.set(null);
    const v = this.formOrden.getRawValue();
    this.ordenes.agregarOrden(idCita, { tipo: v.tipo as Orden['tipo'], descripcion: v.descripcion.trim() }).subscribe({
      next: () => this.terminar('Orden registrada en la atención.'),
      error: (err) => this.fallar(err),
    });
  }

  protected guardarResultado(): void {
    const idOrden = this.ordenActiva();
    if (idOrden === null) return;
    if (this.formResultado.invalid) { this.formResultado.markAllAsTouched(); return; }
    this.guardando.set(true);
    this.error.set(null);
    this.ordenes.registrarResultado(idOrden, this.formResultado.getRawValue().resultado.trim()).subscribe({
      next: () => this.terminar('Resultado registrado. Ya aparece en la historia clínica.'),
      error: (err) => this.fallar(err),
    });
  }

  private terminar(mensaje: string): void {
    this.guardando.set(false);
    this.citaActiva.set(null);
    this.ordenActiva.set(null);
    this.aviso.set(mensaje);
    const p = this.paciente();
    if (p) this.recargar(p.idPaciente);
  }

  private fallar(err: unknown): void {
    this.guardando.set(false);
    this.error.set(mensajeDeError(err));
    // Si hubo un conflicto (p. ej. la atención se cerró), se refresca para mostrar el estado real.
    const p = this.paciente();
    if (p) this.recargar(p.idPaciente);
  }

  private recargar(idPaciente: number): void {
    this.historia.consultarDePaciente(idPaciente).subscribe({
      next: (datos) => {
        this.atenciones.set([...datos].sort((a, b) => b.fecha.localeCompare(a.fecha)));
        this.cargando.set(false);
      },
      error: (err) => { this.cargando.set(false); this.error.set(mensajeDeError(err)); },
    });
  }
}