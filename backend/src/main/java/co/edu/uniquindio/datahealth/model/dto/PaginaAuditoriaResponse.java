package co.edu.uniquindio.datahealth.model.dto;

import java.util.List;

/** Una página de registros de auditoría. Los nombres de los campos son los que lee el frontend. */
public record PaginaAuditoriaResponse(
        List<AuditoriaResponse> contenido,
        int pagina,
        int tamano,
        long total,
        int totalPaginas) {
}