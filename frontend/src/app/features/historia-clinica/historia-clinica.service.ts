import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Atencion } from './historia-clinica.models';

@Injectable({ providedIn: 'root' })
export class HistoriaClinicaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/historia-clinica`;

  /** Paciente/cuidador: el backend resuelve el expediente desde el JWT. SUPUESTO de ruta. */
  consultarPropia(): Observable<Atencion[]> {
    return this.http.get<Atencion[]>(this.url);
  }

  /** Personal médico: historia de un paciente de su misma EPS. SUPUESTO de ruta. */
  consultarDePaciente(idPaciente: number): Observable<Atencion[]> {
    return this.http.get<Atencion[]>(`${environment.apiUrl}/pacientes/${idPaciente}/historia-clinica`);
  }
}
