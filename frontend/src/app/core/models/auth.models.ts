import { Rol } from './rol';

/** LoginRequest del backend. SUPUESTO: se inicia sesión con email + contraseña. */
export interface LoginRequest {
  email: string;
  password: string;
}

/** LoginResponse del backend. SUPUESTO: devuelve al menos el JWT. */
export interface LoginResponse {
  token: string;
}

/**
 * Datos de sesión leídos del JWT (ADR-04: el token lleva usuario, rol e id_eps).
 * Si los nombres de los claims del backend difieren, se ajustan solo en
 * AuthService.decodificar().
 */
export interface Sesion {
  email: string;
  nombre: string;
  rol: Rol;
  idEps: number;
  expiraEn: number; // epoch en milisegundos
}
