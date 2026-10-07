import { Rol } from '../models/rol';

export interface Modulo {
  ruta: string;
  etiqueta: string;
  descripcion: string;
  roles: Rol[];
}

/** Módulos del sistema (M1–M4 del documento de arquitectura) y quién puede entrar. */
export const MODULOS: Modulo[] = [
  {
    ruta: '/pacientes',
    etiqueta: 'Pacientes',
    descripcion: 'Buscar un paciente por documento o registrarlo si es nuevo.',
    roles: ['PERSONAL_MEDICO', 'PERSONAL_ADMINISTRATIVO'],
  },
  {
    ruta: '/ordenes',
    etiqueta: 'Consultas y órdenes',
    descripcion: 'Registrar exámenes, órdenes médicas y resultados de la atención.',
    roles: ['PERSONAL_MEDICO'],
  },
  {
    ruta: '/historia-clinica',
    etiqueta: 'Historia clínica',
    descripcion: 'Ver la línea de tiempo de las atenciones en orden cronológico.',
    roles: ['PERSONAL_MEDICO', 'PACIENTE', 'CUIDADOR'],
  },
  {
    ruta: '/usuarios',
    etiqueta: 'Usuarios',
    descripcion: 'Registrar y gestionar los usuarios de tu entidad.',
    roles: ['ADMIN_ENTIDAD'],
  },
];
