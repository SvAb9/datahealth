export interface RegistroAuditoria {
  id: number;
  usuario: string | null;
  accion: string;
  detalle: string | null;
  fechaHora: string; // ISO
}

export interface PaginaAuditoria {
  contenido: RegistroAuditoria[];
  pagina: number;
  tamano: number;
  total: number;
  totalPaginas: number;
}