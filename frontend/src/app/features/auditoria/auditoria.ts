import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { mensajeDeError } from '../../core/http/api-error';
import { PaginaAuditoria } from './auditoria.models';
import { AuditoriaService } from './auditoria.service';

const ACCIONES: Record<string, string> = {
  CUENTA_BLOQUEADA: 'Cuenta bloqueada',
  LOGIN_BLOQUEADO: 'Intento con cuenta bloqueada',
  LOGIN_FALLIDO: 'Intento fallido de inicio de sesión',
  LOGIN_EXITOSO: 'Inicio de sesión',
  LOGOUT: 'Cierre de sesión',
  USUARIO_REGISTRADO: 'Usuario registrado',
  PACIENTE_REGISTRADO: 'Paciente registrado',
  PACIENTE_AUTOREGISTRADO: 'Paciente creó su propia cuenta',
  HISTORIA_CONSULTADA: 'Historia clínica consultada',
};

const PAGINA_VACIA: PaginaAuditoria = { contenido: [], pagina: 0, tamano: 20, total: 0, totalPaginas: 0 };

/** RNF-15 / RF-05: el administrador consulta quién accedió o modificó información en su entidad. */
@Component({
  selector: 'app-auditoria',
  imports: [DatePipe],
  template: `
    <div class="pagina">
      <h1>Auditoría</h1>
      <p class="pagina__intro">
        Accesos y operaciones de los usuarios de tu entidad, del más reciente al más antiguo.
      </p>

      <div class="panel filtro">
        <div class="campo">
          <label for="accion">Filtrar por acción</label>
          <select id="accion" #sel (change)="filtrar(sel.value)">
            <option value="">Todas</option>
            @for (a of acciones; track a[0]) { <option [value]="a[0]">{{ a[1] }}</option> }
          </select>
        </div>
      </div>

      @if (cargando()) {
        <p role="status">Cargando registros…</p>
      } @else if (error()) {
        <div class="aviso aviso--error" role="alert"><strong>No pudimos cargar la auditoría</strong> {{ error() }}</div>
      } @else if (datos().contenido.length === 0) {
        <div class="vacio">
          <h2>No hay registros</h2>
          <p>No se encontraron eventos con ese filtro.</p>
        </div>
      } @else {
        <div class="tabla-envoltura">
          <table>
            <caption class="solo-lectores">Registros de auditoría de tu entidad</caption>
            <thead>
              <tr>
                <th scope="col">Fecha y hora</th>
                <th scope="col">Usuario</th>
                <th scope="col">Acción</th>
                <th scope="col">Detalle</th>
              </tr>
            </thead>
            <tbody>
              @for (r of datos().contenido; track r.id) {
                <tr [class.alerta]="r.accion === 'CUENTA_BLOQUEADA'">
                  <td>{{ r.fechaHora | date: 'dd/MM/y HH:mm:ss' }}</td>
                  <td>{{ r.usuario ?? '—' }}</td>
                  <td>
                    @if (r.accion === 'CUENTA_BLOQUEADA') { <strong>⚠ {{ nombreAccion(r.accion) }}</strong> }
                    @else { {{ nombreAccion(r.accion) }} }
                  </td>
                  <td>{{ r.detalle ?? '' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <nav class="paginas" aria-label="Paginación">
          <button type="button" class="btn btn--secundario" (click)="irA(datos().pagina - 1)"
                  [disabled]="datos().pagina === 0">Anterior</button>
          <span>Página {{ datos().pagina + 1 }} de {{ datos().totalPaginas }} ({{ datos().total }} registros)</span>
          <button type="button" class="btn btn--secundario" (click)="irA(datos().pagina + 1)"
                  [disabled]="datos().pagina + 1 >= datos().totalPaginas">Siguiente</button>
        </nav>
      }
    </div>
  `,
  styles: `
    .filtro { max-width: 28rem; }
    .filtro .campo { margin-bottom: 0; }
    .tabla-envoltura { overflow-x: auto; background: var(--papel); border: 2px solid var(--linea); border-radius: var(--radio); }
    table { width: 100%; border-collapse: collapse; min-width: 40rem; }
    th, td { text-align: left; padding: 0.7rem 0.9rem; border-bottom: 1px solid var(--linea); vertical-align: top; }
    th { background: var(--niebla); }
    tr.alerta td { background: var(--ladrillo-fondo); }
    .paginas { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem; margin-top: 1.25rem; }
  `,
})
export class Auditoria {
  private readonly servicio = inject(AuditoriaService);

  protected readonly acciones = Object.entries(ACCIONES);
  protected readonly datos = signal<PaginaAuditoria>(PAGINA_VACIA);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  private accion = '';

  constructor() {
    this.cargar(0);
  }

  protected nombreAccion(codigo: string): string {
    return ACCIONES[codigo] ?? codigo;
  }

  protected filtrar(accion: string): void {
    this.accion = accion;
    this.cargar(0);
  }

  protected irA(pagina: number): void {
    this.cargar(pagina);
  }

  private cargar(pagina: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicio.consultar(pagina, this.accion || undefined).subscribe({
      next: (d) => { this.datos.set(d); this.cargando.set(false); },
      error: (err) => { this.error.set(mensajeDeError(err)); this.cargando.set(false); },
    });
  }
}