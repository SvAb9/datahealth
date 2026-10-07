import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NuevoPaciente, Paciente } from './pacientes.models';

@Injectable({ providedIn: 'root' })
export class PacientesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/pacientes`;

  /** RF-06/07. SUPUESTO: responde 404 si el paciente no está registrado en la EPS del usuario. */
  buscarPorDocumento(documento: string): Observable<Paciente> {
    return this.http.get<Paciente>(this.url, { params: new HttpParams().set('documento', documento) });
  }

  /** RF-08/09: el backend rechaza duplicados. */
  registrar(paciente: NuevoPaciente): Observable<Paciente> {
    return this.http.post<Paciente>(this.url, paciente);
  }
}
