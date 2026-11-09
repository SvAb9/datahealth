/** SUPUESTO de contrato: línea de tiempo = citas cerradas + correcciones + resultados (ADR-05). */
export interface Orden {
  tipo: 'MEDICA' | 'MEDICAMENTO' | 'LABORATORIO';
  descripcion: string;
}

export interface CorreccionCita {
  motivo: string;
  usuario: string;
  fechaHora: string;
}

export interface Atencion {
  idCita: number;
  fecha: string; // ISO
  estado: 'ABIERTA' | 'CERRADA';
  medico: string;
  diagnostico?: string;
  ordenes: Orden[];
  correcciones: CorreccionCita[];
  resultados: Resultado[];
}

export interface RegistrarAtencionRequest {
  diagnostico: string;
  observaciones?: string;
}

export interface Resultado {
  idOrden: number;
  resultado: string;
  fechaResultado: string; // ISO
}

export interface Orden {
  idOrden: number;
  tipo: 'MEDICA' | 'MEDICAMENTO' | 'LABORATORIO';
  descripcion: string;
}

