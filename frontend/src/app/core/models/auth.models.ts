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
/** Entidad de salud mostrada en el selector del registro (GET /auth/eps, público). */
export interface Eps {
  idEps: number;
  nombre: string;
}

/** Autorregistro: solo crea pacientes; el backend fija el rol. */
export interface AutorregistroPacienteRequest {
  idEps: number;
  documento: string;
  nombre: string;
  edad: number;
  telefono: string;
  direccion?: string;
  email: string;
  password: string;
}