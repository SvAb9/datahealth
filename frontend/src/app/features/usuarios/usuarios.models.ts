import { Rol } from '../../core/models/rol';

/**
 * RegistrarUsuarioRequest del backend. SUPUESTO de campos.
 * No se envía id_eps: el backend lo toma del JWT (ADR-03).
 */
export interface RegistrarUsuarioRequest {
  nombre: string;
  email: string;
  password: string;
  rol: Rol;
}

/** UsuarioResponse del backend. SUPUESTO de campos. */
export interface UsuarioResponse {
  idUsuario: number;
  nombre: string;
  email: string;
  rol: Rol;
  activo: boolean;
}
