package co.edu.uniquindio.datahealth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.datahealth.model.dto.PaginaAuditoriaResponse;
import co.edu.uniquindio.datahealth.service.AuditoriaService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
@Validated
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    /** Solo el administrador de entidad consulta la auditoría de su EPS (RF-05, RNF-15). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_ENTIDAD')")
    public ResponseEntity<PaginaAuditoriaResponse> consultar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @Size(max = 50) String accion) {
        return ResponseEntity.ok(auditoriaService.consultar(page, size, accion));
    }
}