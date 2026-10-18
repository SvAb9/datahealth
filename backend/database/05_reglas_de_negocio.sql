-- =====================================================================
-- Data-Health · 05 · Reglas de integridad en la base de datos (RN-02, ADR-05)
-- Ejecutar conectado como DATAHEALTH, DESPUÉS del script 04 (los datos de demo
-- cierran las citas con UPDATE, algo que estos triggers prohíben después).
--
-- La capa Service ya valida estas reglas (responde 409). Estos triggers son la
-- segunda barrera: protegen aunque alguien escriba directo en la base de datos.
-- =====================================================================

-- Una cita nunca se borra; una cita CERRADA no se modifica.
CREATE OR REPLACE TRIGGER trg_cita_inmutable
BEFORE UPDATE OR DELETE ON cita
FOR EACH ROW
BEGIN
  IF DELETING THEN
    RAISE_APPLICATION_ERROR(-20010, 'Las citas no se pueden eliminar.');
  END IF;
  IF :OLD.estado = 'CERRADA' THEN
    RAISE_APPLICATION_ERROR(-20011, 'La cita esta cerrada y no se puede modificar. Registre una correccion.');
  END IF;
END;
/

-- No se agregan, cambian ni borran órdenes de una cita cerrada.
CREATE OR REPLACE TRIGGER trg_orden_cita_cerrada
BEFORE INSERT OR UPDATE OR DELETE ON orden
FOR EACH ROW
DECLARE
  v_estado cita.estado%TYPE;
  v_cita   NUMBER;
BEGIN
  IF DELETING THEN
    RAISE_APPLICATION_ERROR(-20012, 'Las ordenes no se pueden eliminar.');
  END IF;
  v_cita := :NEW.id_cita;
  SELECT estado INTO v_estado FROM cita WHERE id_cita = v_cita;
  IF v_estado = 'CERRADA' THEN
    RAISE_APPLICATION_ERROR(-20013, 'La cita esta cerrada: no se pueden agregar ni modificar ordenes.');
  END IF;
END;
/

-- Igual para los exámenes que aporta el paciente.
CREATE OR REPLACE TRIGGER trg_examen_cita_cerrada
BEFORE INSERT OR UPDATE OR DELETE ON examen_paciente
FOR EACH ROW
DECLARE
  v_estado cita.estado%TYPE;
  v_cita   NUMBER;
BEGIN
  IF DELETING THEN
    RAISE_APPLICATION_ERROR(-20014, 'Los examenes no se pueden eliminar.');
  END IF;
  v_cita := :NEW.id_cita;
  SELECT estado INTO v_estado FROM cita WHERE id_cita = v_cita;
  IF v_estado = 'CERRADA' THEN
    RAISE_APPLICATION_ERROR(-20015, 'La cita esta cerrada: no se pueden agregar ni modificar examenes.');
  END IF;
END;
/

-- Las correcciones solo se insertan, y solo sobre citas ya cerradas.
CREATE OR REPLACE TRIGGER trg_correccion_solo_insercion
BEFORE INSERT OR UPDATE OR DELETE ON correccion_cita
FOR EACH ROW
DECLARE
  v_estado cita.estado%TYPE;
BEGIN
  IF UPDATING OR DELETING THEN
    RAISE_APPLICATION_ERROR(-20016, 'Las correcciones son de solo insercion.');
  END IF;
  SELECT estado INTO v_estado FROM cita WHERE id_cita = :NEW.id_cita;
  IF v_estado <> 'CERRADA' THEN
    RAISE_APPLICATION_ERROR(-20017, 'Solo se corrigen citas cerradas; una cita abierta se edita directamente.');
  END IF;
END;
/

-- Los resultados (que pueden llegar tras el cierre) tampoco se editan ni se borran.
CREATE OR REPLACE TRIGGER trg_resultado_solo_insercion
BEFORE UPDATE OR DELETE ON resultado_examen
BEGIN
  RAISE_APPLICATION_ERROR(-20018, 'Los resultados de examen son de solo insercion.');
END;
/