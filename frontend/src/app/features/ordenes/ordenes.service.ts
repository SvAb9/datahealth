import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Orden, Resultado } from '../historia-clinica/historia-clinica.models';

export interface NuevaOrden {
  tipo: Orden['tipo'];
  descripcion: string;
}

@Injectable({ providedIn: 'root' })
export class OrdenesService {
  private readonly http = inject(HttpClient);

  /** CA-01: el backend vincula la orden a la cita y toma la EPS del JWT. */
  agregarOrden(idCita: number, orden: NuevaOrden): Observable<Orden> {
    return this.http.post<Orden>(`${environment.apiUrl}/citas/${idCita}/ordenes`, orden);
  }

  /** CA-02: el resultado puede cargarse aunque la atención ya esté cerrada. */
  registrarResultado(idOrden: number, resultado: string): Observable<Resultado> {
    return this.http.post<Resultado>(`${environment.apiUrl}/ordenes/${idOrden}/resultados`, { resultado });
  }
}