-- Verificación rápida (conectado como DATAHEALTH). Ejecutar con F5.

-- 1) Deben aparecer 3 tablas: EPS, USUARIO, REGISTRO_AUDITORIA
SELECT table_name FROM user_tables ORDER BY table_name;

-- 2) Debe aparecer 1 EPS y 1 usuario ADMIN_ENTIDAD
SELECT * FROM eps;
SELECT id_usuario, id_eps, email, rol, activo FROM usuario;

-- 3) Versión de Oracle (si dice 23ai, lee la nota sobre BOOLEAN en la guía)
SELECT product, version FROM product_component_version;
