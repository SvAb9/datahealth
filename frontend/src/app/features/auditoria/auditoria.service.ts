import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PaginaAuditoria } from './auditoria.models';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/auditoria`;

  consultar(pagina: number, accion?: string): Observable<PaginaAuditoria> {
    let params = new HttpParams().set('page', pagina).set('size', 20);
    if (accion) params = params.set('accion', accion);
    return this.http.get<PaginaAuditoria>(this.url, { params });
  }
}