-- Verificación rápida (conectado como DATAHEALTH). Ejecutar con F5.

-- 1) Deben aparecer 9 tablas: EPS, USUARIO, REGISTRO_AUDITORIA
SELECT table_name FROM user_tables ORDER BY table_name;

-- 2) Debe aparecer 1 EPS y 1 usuario ADMIN_ENTIDAD
SELECT * FROM eps;
SELECT id_usuario, id_eps, email, rol, activo FROM usuario ORDER BY id_usuario;

-- 3) Versión de Oracle. En Autonomous elige 19c al crear la base; con 23ai/26ai
--    Hibernate puede rechazar las columnas NUMBER(1) al validar el esquema.
SELECT product, version FROM product_component_version;

-- 4) Deben aparecer 6 triggers en estado ENABLED (1 de auditoría y 5 del script 05)
SELECT trigger_name, status FROM user_triggers ORDER BY trigger_name;
