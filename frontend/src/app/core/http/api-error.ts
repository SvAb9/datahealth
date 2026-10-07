import { HttpErrorResponse } from '@angular/common/http';

/** Formato uniforme de error del backend (ADR-07): fecha, estado, mensaje y ruta. */
export interface ApiError {
  fecha: string;
  estado: number;
  mensaje: string;
  ruta: string;
}

export function mensajeDeError(
  err: unknown,
  porDefecto = 'No pudimos completar la acción. Intenta de nuevo.',
): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'No hay conexión con el servidor. Revisa tu internet e intenta de nuevo.';
    const cuerpo = err.error as Partial<ApiError> | null;
    if (cuerpo?.mensaje) return cuerpo.mensaje;
  }
  return porDefecto;
}
