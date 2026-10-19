




-- =====================================================================
-- Data-Health · 00 · Usuario para Oracle Autonomous Database (Always Free)
-- Ejecutar conectado como ADMIN. Reemplaza al 00_crear_usuario.sql, que es
-- para Oracle XE (usa tablespaces USERS/TEMP que no existen en Autonomous).
--
-- Cambia la contraseña antes de ejecutar. En Autonomous debe tener de 12 a 30
-- caracteres, con mayúscula, minúscula y número, sin comillas dobles ni el nombre del usuario.
-- =====================================================================
CREATE USER datahealth IDENTIFIED BY "Reeplazar contrasenia aqui"
  QUOTA UNLIMITED ON data;

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER, CREATE VIEW TO datahealth;

--CREATE USER datahealth IDENTIFIED BY "DataHealth123"
--  DEFAULT TABLESPACE users
--  TEMPORARY TABLESPACE temp
--  QUOTA UNLIMITED ON users;

--GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER, CREATE VIEW TO datahealth;
