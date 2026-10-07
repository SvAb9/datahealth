/** PacienteDTO del backend. SUPUESTO de campos (RF-09). La EPS la asigna el backend (RF-10). */
export interface Paciente {
  idPaciente: number;
  documento: string;
  nombre: string;
  edad: number;
  telefono: string;
  direccion?: string;
  eps?: string;
}

export type NuevoPaciente = Omit<Paciente, 'idPaciente' | 'eps'>;
