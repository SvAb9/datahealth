-- =====================================================================
-- Data-Health · 98 · Reiniciar la parte clínica y los datos de demo
-- SOLO DESARROLLO / PRUEBAS. NUNCA en producción: borra información clínica.
-- Conectado como DATAHEALTH. Después vuelve a ejecutar 03, 04 y 05.
-- Conserva eps "EPS Demo", admin@demo.com y registro_auditoria (que es de solo inserción).
-- =====================================================================

DROP TABLE resultado_examen CASCADE CONSTRAINTS PURGE;
DROP TABLE correccion_cita  CASCADE CONSTRAINTS PURGE;
DROP TABLE examen_paciente  CASCADE CONSTRAINTS PURGE;
DROP TABLE orden            CASCADE CONSTRAINTS PURGE;
DROP TABLE cita             CASCADE CONSTRAINTS PURGE;
DROP TABLE paciente         CASCADE CONSTRAINTS PURGE;

ALTER TABLE usuario DROP CONSTRAINT uq_usuario_eps_id;

DELETE FROM usuario
 WHERE (email LIKE '%@demo.com' AND email <> 'admin@demo.com')
    OR email LIKE '%@saludvida.com';
DELETE FROM eps WHERE nombre = 'Salud Vida EPS';

COMMIT;