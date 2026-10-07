import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { mensajeDeError } from '../../core/http/api-error';
import { Atencion } from './historia-clinica.models';
import { HistoriaClinicaService } from './historia-clinica.service';

const ETIQUETA_ORDEN = { MEDICA: 'Orden médica', MEDICAMENTO: 'Medicamento', LABORATORIO: 'Laboratorio' } as const;

/** RF-19: historia clínica como línea de tiempo de citas, de la más reciente a la más antigua. */
@Component({
  selector: 'app-linea-tiempo',
  imports: [DatePipe],
  template: `
    <div class="pagina">
      <h1>Historia clínica</h1>
      <p class="pagina__intro">Atenciones registradas, de la más reciente a la más antigua.</p>

      @if (cargando()) {
        <p role="status">Cargando historia clínica…</p>
      } @else if (error()) {
        <div class="aviso aviso--error" role="alert"><strong>No pudimos cargar la historia clínica</strong> {{ error() }}</div>
      } @else if (atenciones().length === 0) {
        <div class="vacio">
          <h2>Aún no hay atenciones</h2>
          <p>Cuando se cierre una atención aparecerá aquí.</p>
        </div>
      } @else {
        <ol class="tiempo">
          @for (a of atenciones(); track a.idCita) {
            <li>
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
                  @for (o of a.ordenes; track $index) { <li><strong>{{ etiquetaOrden[o.tipo] }}:</strong> {{ o.descripcion }}</li> }
                </ul>
              }
              @for (c of a.correcciones; track $index) {
                <p class="correccion"><strong>Corrección posterior</strong> ({{ c.fechaHora | date: 'dd/MM/y HH:mm' }}, {{ c.usuario }}): {{ c.motivo }}</p>
              }
            </li>
          }
        </ol>
      }
    </div>
  `,
  styles: `
    .tiempo { list-style: none; margin: 0; padding: 0 0 0 1.75rem; border-left: 0.3rem solid var(--puerto); }
    .tiempo > li { position: relative; padding-bottom: 2rem; }
    .tiempo > li::before { content: ''; position: absolute; left: -2.2rem; top: 0.45rem; width: 1rem; height: 1rem;
                           border-radius: 50%; background: var(--papel); border: 0.3rem solid var(--puerto); }
    .meta { color: var(--tinta-suave); }
    .estado { display: inline-block; margin-right: 0.75rem; padding: 0.1rem 0.7rem; font-weight: 700; border: 2px solid var(--tinta-suave); border-radius: 2rem; }
    .estado--cerrada { border-color: var(--musgo); color: var(--musgo); }
    .ordenes { margin: 0 0 1rem; padding-left: 1.25rem; }
    .correccion { background: var(--papel); border-left: 0.4rem solid var(--puerto); padding: 0.6rem 0.9rem; }
  `,
})
export class LineaTiempo {
  private readonly servicio = inject(HistoriaClinicaService);

  /** Viene de la ruta :idPaciente (withComponentInputBinding). Sin id = historia propia. */
  readonly idPaciente = input<string>();

  protected readonly etiquetaOrden = ETIQUETA_ORDEN;
  protected readonly atenciones = signal<Atencion[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.idPaciente();
      this.cargando.set(true);
      this.error.set(null);
      const peticion = id ? this.servicio.consultarDePaciente(Number(id)) : this.servicio.consultarPropia();
      peticion.subscribe({
        next: (datos) => {
          this.atenciones.set([...datos].sort((a, b) => b.fecha.localeCompare(a.fecha)));
          this.cargando.set(false);
        },
        error: (err) => { this.error.set(mensajeDeError(err)); this.cargando.set(false); },
      });
    });
  }
}
