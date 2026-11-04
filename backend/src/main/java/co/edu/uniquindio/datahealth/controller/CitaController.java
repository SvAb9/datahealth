package co.edu.uniquindio.datahealth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.datahealth.model.dto.AtencionResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarCitaRequest;
import co.edu.uniquindio.datahealth.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    /** Solo el personal médico registra información clínica (RF-04). */
    @PostMapping("/api/v1/pacientes/{idPaciente}/citas")
    @PreAuthorize("hasRole('PERSONAL_MEDICO')")
    public ResponseEntity<AtencionResponse> registrar(@PathVariable Long idPaciente,
                                                      @Valid @RequestBody RegistrarCitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.registrar(idPaciente, request));
    }
}