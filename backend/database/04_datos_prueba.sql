-- =====================================================================
-- Data-Health · 04 · Datos de DEMOSTRACIÓN (solo ambientes de prueba)
-- Ejecutar conectado como DATAHEALTH, DESPUÉS de 01, 02 y 03, y ANTES de 05.
-- NO ejecutar en producción: todos los usuarios comparten la misma clave.
--
-- Clave de TODOS los usuarios de prueba:  Admin12345*
--
-- EPS Demo
--   admin@demo.com          Administrador de entidad (ya creado por el script 02)
--   medico@demo.com         Personal médico (Dra. Laura Gómez)
--   medico2@demo.com        Personal médico (Dr. Carlos Ramírez)
--   recepcion@demo.com      Personal administrativo
--   paciente@demo.com       Paciente Jorge Hernández (doc. 10203040)
--   paciente2@demo.com      Paciente Rosa Martínez   (doc. 20304050)
--   bloqueado@demo.com      Cuenta bloqueada 24 h (probar mensaje de bloqueo, RF-05)
--   inactivo@demo.com       Cuenta dada de baja (se trata como credenciales inválidas)
-- Salud Vida EPS (segunda entidad, para probar el aislamiento, RN-01)
--   admin@saludvida.com, medico@saludvida.com, recepcion@saludvida.com, paciente@saludvida.com
--
-- Es seguro ejecutarlo una sola vez; si ya se cargó, no hace nada.
-- =====================================================================

SET SERVEROUTPUT ON

DECLARE
  -- Hash BCrypt de "Admin12345*" (el mismo del script 02).
  c_hash CONSTANT VARCHAR2(100) := '$2a$10$C/0HcKhbaKRezRx5lWzo2ekAc325kph.sLCM4ETtfAFkvmy1RJPMu';

  v_existe NUMBER;
  v_eps1 NUMBER; v_eps2 NUMBER;
  v_admin1 NUMBER; v_admin2 NUMBER;
  v_med1 NUMBER; v_med2 NUMBER; v_med3 NUMBER;
  v_rec1 NUMBER; v_rec2 NUMBER;
  v_pu1 NUMBER; v_pu2 NUMBER; v_pu3 NUMBER;
  v_p1 NUMBER; v_p2 NUMBER; v_p3 NUMBER; v_p4 NUMBER; v_p5 NUMBER; v_p6 NUMBER;
  v_c NUMBER; v_o NUMBER; v_dummy NUMBER;

  FUNCTION hace(p_dias NUMBER) RETURN TIMESTAMP IS
  BEGIN
    RETURN LOCALTIMESTAMP - NUMTODSINTERVAL(p_dias, 'DAY');
  END;

  FUNCTION mk_usuario(p_eps NUMBER, p_nombre VARCHAR2, p_email VARCHAR2, p_rol VARCHAR2,
                      p_activo NUMBER DEFAULT 1, p_intentos NUMBER DEFAULT 0,
                      p_bloq TIMESTAMP DEFAULT NULL, p_creador NUMBER DEFAULT NULL) RETURN NUMBER IS
    v_id NUMBER;
  BEGIN
    INSERT INTO usuario (id_eps, nombre, email, password_hash, rol, activo, intentos_fallidos,
                         bloqueado_hasta, creado_por, fecha_creacion)
    VALUES (p_eps, p_nombre, p_email, c_hash, p_rol, p_activo, p_intentos, p_bloq, p_creador, hace(60))
    RETURNING id_usuario INTO v_id;
    RETURN v_id;
  END;

  FUNCTION mk_paciente(p_eps NUMBER, p_usuario NUMBER, p_doc VARCHAR2, p_nombre VARCHAR2, p_edad NUMBER,
                       p_tel VARCHAR2, p_dir VARCHAR2, p_creador NUMBER) RETURN NUMBER IS
    v_id NUMBER;
  BEGIN
    INSERT INTO paciente (id_eps, id_usuario, documento, nombre, edad, telefono, direccion,
                          activo, creado_por, fecha_creacion)
    VALUES (p_eps, p_usuario, p_doc, p_nombre, p_edad, p_tel, p_dir, 1, p_creador, hace(60))
    RETURNING id_paciente INTO v_id;
    RETURN v_id;
  END;

  -- La cita nace ABIERTA; se cierra con cerrar() una vez cargadas sus órdenes.
  FUNCTION mk_cita(p_paciente NUMBER, p_medico NUMBER, p_dias NUMBER,
                   p_diag VARCHAR2, p_obs VARCHAR2) RETURN NUMBER IS
    v_id NUMBER; v_eps NUMBER;
  BEGIN
    SELECT id_eps INTO v_eps FROM paciente WHERE id_paciente = p_paciente;
    INSERT INTO cita (id_eps, id_paciente, id_medico, fecha, estado, diagnostico, observaciones,
                      creado_por, fecha_creacion)
    VALUES (v_eps, p_paciente, p_medico, hace(p_dias), 'ABIERTA', p_diag, p_obs, p_medico, hace(p_dias))
    RETURNING id_cita INTO v_id;
    RETURN v_id;
  END;

  FUNCTION mk_orden(p_cita NUMBER, p_tipo VARCHAR2, p_desc VARCHAR2, p_usuario NUMBER) RETURN NUMBER IS
    v_id NUMBER; v_eps NUMBER; v_fecha TIMESTAMP;
  BEGIN
    SELECT id_eps, fecha INTO v_eps, v_fecha FROM cita WHERE id_cita = p_cita;
    INSERT INTO orden (id_eps, id_cita, tipo, descripcion, creado_por, fecha_creacion)
    VALUES (v_eps, p_cita, p_tipo, p_desc, p_usuario, v_fecha)
    RETURNING id_orden INTO v_id;
    RETURN v_id;
  END;

  PROCEDURE mk_examen(p_cita NUMBER, p_nombre VARCHAR2, p_desc VARCHAR2, p_usuario NUMBER) IS
    v_eps NUMBER; v_fecha TIMESTAMP;
  BEGIN
    SELECT id_eps, fecha INTO v_eps, v_fecha FROM cita WHERE id_cita = p_cita;
    INSERT INTO examen_paciente (id_eps, id_cita, nombre, descripcion, fecha_examen, creado_por, fecha_creacion)
    VALUES (v_eps, p_cita, p_nombre, p_desc, CAST(v_fecha AS DATE) - 10, p_usuario, v_fecha);
  END;

  PROCEDURE cerrar(p_cita NUMBER, p_medico NUMBER) IS
  BEGIN
    UPDATE cita
       SET estado = 'CERRADA',
           fecha_cierre = fecha + NUMTODSINTERVAL(45, 'MINUTE'),
           cerrado_por = p_medico,
           modificado_por = p_medico,
           fecha_modificacion = fecha + NUMTODSINTERVAL(45, 'MINUTE')
     WHERE id_cita = p_cita;
  END;

  -- Los resultados pueden llegar después del cierre (ADR-05).
  PROCEDURE mk_resultado(p_orden NUMBER, p_texto VARCHAR2, p_dias NUMBER, p_usuario NUMBER) IS
    v_eps NUMBER; v_cita NUMBER;
  BEGIN
    SELECT id_eps, id_cita INTO v_eps, v_cita FROM orden WHERE id_orden = p_orden;
    INSERT INTO resultado_examen (id_eps, id_orden, id_cita, resultado, fecha_resultado, creado_por, fecha_creacion)
    VALUES (v_eps, p_orden, v_cita, p_texto, hace(p_dias), p_usuario, hace(p_dias));
  END;

  PROCEDURE mk_correccion(p_cita NUMBER, p_usuario NUMBER, p_motivo VARCHAR2, p_detalle VARCHAR2, p_dias NUMBER) IS
    v_eps NUMBER;
  BEGIN
    SELECT id_eps INTO v_eps FROM cita WHERE id_cita = p_cita;
    INSERT INTO correccion_cita (id_eps, id_cita, motivo, detalle, id_usuario, fecha_hora)
    VALUES (v_eps, p_cita, p_motivo, p_detalle, p_usuario, hace(p_dias));
  END;

  PROCEDURE aud(p_eps NUMBER, p_usuario VARCHAR2, p_accion VARCHAR2, p_detalle VARCHAR2, p_dias NUMBER) IS
  BEGIN
    INSERT INTO registro_auditoria (id_eps, usuario, accion, detalle, fecha_hora)
    VALUES (p_eps, p_usuario, p_accion, p_detalle, hace(p_dias));
  END;

BEGIN
  SELECT COUNT(*) INTO v_existe FROM usuario WHERE email = 'medico@demo.com';
  IF v_existe > 0 THEN
    DBMS_OUTPUT.PUT_LINE('Los datos de demostración ya estaban cargados. No se hizo nada.');
    RETURN;
  END IF;

  SELECT id_eps INTO v_eps1 FROM eps WHERE nombre = 'EPS Demo';
  SELECT id_usuario INTO v_admin1 FROM usuario WHERE email = 'admin@demo.com';

  INSERT INTO eps (nombre, activa) VALUES ('Salud Vida EPS', 1) RETURNING id_eps INTO v_eps2;

  -- ===================== USUARIOS · EPS Demo =====================
  v_med1 := mk_usuario(v_eps1, 'Dra. Laura Gómez Restrepo', 'medico@demo.com',    'PERSONAL_MEDICO',        p_creador => v_admin1);
  v_med2 := mk_usuario(v_eps1, 'Dr. Carlos Ramírez Ocampo', 'medico2@demo.com',   'PERSONAL_MEDICO',        p_creador => v_admin1);
  v_rec1 := mk_usuario(v_eps1, 'Marta Rojas Villegas',      'recepcion@demo.com', 'PERSONAL_ADMINISTRATIVO', p_creador => v_admin1);
  v_pu1  := mk_usuario(v_eps1, 'Jorge Hernández Salazar',   'paciente@demo.com',  'PACIENTE',               p_creador => v_admin1);
  v_pu2  := mk_usuario(v_eps1, 'Rosa Martínez de Cardona',  'paciente2@demo.com', 'PACIENTE',               p_creador => v_admin1);
  v_dummy := mk_usuario(v_eps1, 'Pedro Bloqueado Prueba',   'bloqueado@demo.com', 'PERSONAL_MEDICO',
                        p_intentos => 3, p_bloq => LOCALTIMESTAMP + NUMTODSINTERVAL(1, 'DAY'), p_creador => v_admin1);
  v_dummy := mk_usuario(v_eps1, 'Inés Inactiva Prueba',     'inactivo@demo.com',  'PERSONAL_ADMINISTRATIVO',
                        p_activo => 0, p_creador => v_admin1);

  -- ===================== USUARIOS · Salud Vida EPS =====================
  v_admin2 := mk_usuario(v_eps2, 'Administrador Salud Vida', 'admin@saludvida.com',     'ADMIN_ENTIDAD');
  v_med3   := mk_usuario(v_eps2, 'Dr. Andrés Mejía Pineda',  'medico@saludvida.com',    'PERSONAL_MEDICO',        p_creador => v_admin2);
  v_rec2   := mk_usuario(v_eps2, 'Paula Castaño Ríos',       'recepcion@saludvida.com', 'PERSONAL_ADMINISTRATIVO', p_creador => v_admin2);
  v_pu3    := mk_usuario(v_eps2, 'Gloria Patiño Arias',      'paciente@saludvida.com',  'PACIENTE',               p_creador => v_admin2);

  -- ===================== PACIENTES =====================
  v_p1 := mk_paciente(v_eps1, v_pu1,  '10203040', 'Jorge Hernández Salazar',  78, '3101234567', 'Carrera 14 # 12-30, Armenia', v_rec1);
  v_p2 := mk_paciente(v_eps1, v_pu2,  '20304050', 'Rosa Martínez de Cardona', 82, '3157654321', 'Calle 21 # 15-40, Armenia',   v_rec1);
  v_p3 := mk_paciente(v_eps1, NULL,   '30405060', 'Alberto Quintero Lozano',  71, '3209876543', NULL,                          v_rec1); -- sin atenciones
  v_p4 := mk_paciente(v_eps1, NULL,   '40506070', 'Luz Marina Giraldo Henao', 69, '3123456789', 'Barrio La Patria, Armenia',   v_rec1);
  v_p5 := mk_paciente(v_eps2, v_pu3,  '50607080', 'Gloria Patiño Arias',      75, '3186543210', 'Avenida Bolívar # 8-12, Calarcá', v_rec2);
  v_p6 := mk_paciente(v_eps2, NULL,   '70809010', 'Hernán Duque Marín',       80, '3001112233', NULL,                          v_rec2);

  -- ===================== HISTORIA · Jorge Hernández (4 atenciones) =====================
  -- 1) Hace 120 días: cerrada, con exámenes aportados
  v_c := mk_cita(v_p1, v_med2, 120, 'Hipertensión arterial esencial, controlada.',
                 'Paciente asintomático. Presión arterial 138/86 mmHg.');
  mk_examen(v_c, 'Electrocardiograma', 'Ritmo sinusal, sin alteraciones agudas.', v_med2);
  v_dummy := mk_orden(v_c, 'MEDICAMENTO', 'Losartán 50 mg, una tableta cada 12 horas durante 90 días.', v_med2);
  v_o     := mk_orden(v_c, 'LABORATORIO', 'Perfil lipídico y creatinina sérica en ayunas.', v_med2);
  v_dummy := mk_orden(v_c, 'MEDICA',      'Control en 3 meses con medicina interna.', v_med2);
  cerrar(v_c, v_med2);
  mk_resultado(v_o, 'Creatinina 1.0 mg/dL (normal). Colesterol total 232 mg/dL, LDL 148 mg/dL, HDL 42 mg/dL.', 118, v_med2);

  -- 2) Hace 60 días: cerrada, con una corrección posterior (ADR-05)
  v_c := mk_cita(v_p1, v_med1, 60, 'Dislipidemia mixta.',
                 'Refiere dolor leve en miembros inferiores al caminar. Se ajusta tratamiento.');
  v_dummy := mk_orden(v_c, 'MEDICAMENTO', 'Atorvastatina 20 mg, una tableta en la noche.', v_med1);
  v_o     := mk_orden(v_c, 'LABORATORIO', 'Hemograma, glucosa en ayunas y hemoglobina glicosilada.', v_med1);
  cerrar(v_c, v_med1);
  mk_resultado(v_o, 'Glucosa 118 mg/dL. HbA1c 6.1 %. Hemograma sin alteraciones.', 55, v_med1);
  mk_correccion(v_c, v_med1, 'Corrección de dosis registrada',
                'La dosis correcta de atorvastatina es 40 mg, no 20 mg.', 58);

  -- 3) Hace 7 días: cerrada
  v_c := mk_cita(v_p1, v_med1, 7, 'Prediabetes en seguimiento.',
                 'Se refuerza plan de alimentación y actividad física.');
  v_dummy := mk_orden(v_c, 'MEDICA',      'Valoración por nutrición.', v_med1);
  v_dummy := mk_orden(v_c, 'LABORATORIO', 'Curva de tolerancia a la glucosa.', v_med1);
  cerrar(v_c, v_med1);

  -- 4) Hoy: ABIERTA (para ver el estado "Atención abierta")
  v_c := mk_cita(v_p1, v_med2, 0.05, NULL, 'Paciente refiere mareos ocasionales al levantarse.');
  v_dummy := mk_orden(v_c, 'LABORATORIO', 'Hemograma completo y electrolitos.', v_med2);

  -- ===================== HISTORIA · Rosa Martínez (2 atenciones) =====================
  v_c := mk_cita(v_p2, v_med1, 90, 'Osteoartrosis de rodilla bilateral.',
                 'Dolor al subir escaleras. Sin signos de inflamación aguda.');
  v_dummy := mk_orden(v_c, 'MEDICAMENTO', 'Acetaminofén 500 mg cada 8 horas durante 15 días.', v_med1);
  v_dummy := mk_orden(v_c, 'MEDICA',      'Remisión a fisioterapia: 10 sesiones.', v_med1);
  cerrar(v_c, v_med1);

  v_c := mk_cita(v_p2, v_med2, 14, 'Osteoporosis en control.',
                 'Sin fracturas recientes. Buena adherencia al tratamiento.');
  mk_examen(v_c, 'Radiografía de columna lumbar', 'Disminución de la densidad ósea.', v_med2);
  v_o     := mk_orden(v_c, 'LABORATORIO', 'Densitometría ósea.', v_med2);
  v_dummy := mk_orden(v_c, 'MEDICAMENTO', 'Calcio con vitamina D, una tableta al día.', v_med2);
  cerrar(v_c, v_med2);
  mk_resultado(v_o, 'Densitometría: T-score -2.7 en columna lumbar (osteoporosis).', 10, v_med2);

  -- ===================== HISTORIA · Luz Marina Giraldo (1 atención) =====================
  v_c := mk_cita(v_p4, v_med1, 30, 'Hipotiroidismo primario.',
                 'Fatiga y aumento de peso en los últimos meses.');
  v_dummy := mk_orden(v_c, 'MEDICAMENTO', 'Levotiroxina 50 mcg en ayunas, todos los días.', v_med1);
  v_o     := mk_orden(v_c, 'LABORATORIO', 'TSH y T4 libre.', v_med1);
  cerrar(v_c, v_med1);
  mk_resultado(v_o, 'TSH 8.4 mUI/L (elevada). T4 libre 0.7 ng/dL (baja).', 27, v_med1);

  -- ===================== HISTORIA · Salud Vida EPS =====================
  v_c := mk_cita(v_p5, v_med3, 20, 'Enfermedad pulmonar obstructiva crónica (EPOC) estable.',
                 'Disnea de esfuerzo moderado. Saturación 93 % al aire ambiente.');
  v_dummy := mk_orden(v_c, 'MEDICAMENTO', 'Salbutamol inhalador, 2 disparos cada 6 horas si hay síntomas.', v_med3);
  v_dummy := mk_orden(v_c, 'LABORATORIO', 'Espirometría con broncodilatador.', v_med3);
  cerrar(v_c, v_med3);

  -- ===================== AUDITORÍA (alertas para el administrador, RF-05) =====================
  aud(v_eps1, 'medico@demo.com',     'LOGIN_EXITOSO',   NULL, 3);
  aud(v_eps1, 'recepcion@demo.com',  'LOGIN_EXITOSO',   NULL, 2);
  aud(v_eps1, 'bloqueado@demo.com',  'LOGIN_FALLIDO',   'Intento 1 de 3', 0.02);
  aud(v_eps1, 'bloqueado@demo.com',  'LOGIN_FALLIDO',   'Intento 2 de 3', 0.019);
  aud(v_eps1, 'bloqueado@demo.com',  'CUENTA_BLOQUEADA','Bloqueo por 3 intentos fallidos consecutivos', 0.018);
  aud(v_eps1, 'admin@demo.com',      'USUARIO_REGISTRADO', 'Registró a medico@demo.com con rol PERSONAL_MEDICO', 60);
  aud(v_eps2, 'medico@saludvida.com','LOGIN_EXITOSO',   NULL, 1);
  aud(NULL,   'desconocido@x.com',   'LOGIN_FALLIDO',   'Correo no registrado', 1);

  COMMIT;
  DBMS_OUTPUT.PUT_LINE('Datos de demostración cargados. Clave de todos los usuarios: Admin12345*');
EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    RAISE;
END;
/