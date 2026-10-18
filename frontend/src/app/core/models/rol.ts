/** Espejo del enum Rol del backend (model/enums/Rol.java). */
export type Rol =
  | 'ADMIN_ENTIDAD'
  | 'PERSONAL_MEDICO'
  | 'PERSONAL_ADMINISTRATIVO'
  | 'PACIENTE'
  ;

export const ETIQUETA_ROL: Record<Rol, string> = {
  ADMIN_ENTIDAD: 'Administrador de entidad',
  PERSONAL_MEDICO: 'Personal médico',
  PERSONAL_ADMINISTRATIVO: 'Personal administrativo',
  PACIENTE: 'Paciente',
};
