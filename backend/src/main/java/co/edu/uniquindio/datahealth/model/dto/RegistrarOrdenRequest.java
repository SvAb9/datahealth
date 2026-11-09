package co.edu.uniquindio.datahealth.model.dto;

import co.edu.uniquindio.datahealth.model.enums.TipoOrden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** No lleva id_eps ni id_cita en el cuerpo: la EPS sale del JWT y la cita de la ruta (ADR-03). */
public record RegistrarOrdenRequest(
        @NotNull(message = "Elige el tipo de orden.")
        TipoOrden tipo,

        @NotBlank(message = "Escribe la descripción de la orden.")
        @Size(max = 1000, message = "La descripción es demasiado larga (máximo 1000 caracteres).")
        String descripcion) {
}