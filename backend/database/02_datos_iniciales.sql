-- =====================================================================
-- Data-Health · 02 · Datos iniciales (una EPS de demostración y un administrador)
-- Ejecutar conectado como DATAHEALTH, DESPUÉS del script 01.
--
-- Credenciales de prueba:  admin@demo.com  /  Admin12345*
-- El hash es BCrypt (ADR-04). Cámbialo en cualquier ambiente real.
-- Si más adelante implementan un DataSeeder en Java, no dupliquen este usuario (email es único).
-- =====================================================================

INSERT INTO eps (nombre, activa) VALUES ('EPS Demo', 1);

INSERT INTO usuario (id_eps, nombre, email, password_hash, rol, activo, intentos_fallidos, fecha_creacion)
VALUES (
  (SELECT id_eps FROM eps WHERE nombre = 'EPS Demo'),
  'Administrador Demo',
  'admin@demo.com',
  '$2a$10$C/0HcKhbaKRezRx5lWzo2ekAc325kph.sLCM4ETtfAFkvmy1RJPMu',
  'ADMIN_ENTIDAD',
  1,
  0,
  SYSTIMESTAMP
);

-- SQL Developer NO confirma los cambios automáticamente: sin COMMIT la aplicación no verá estos datos.
COMMIT;
