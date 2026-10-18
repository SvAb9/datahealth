package co.edu.uniquindio.datahealth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.datahealth.model.dto.AtencionResponse;
import co.edu.uniquindio.datahealth.service.HistoriaClinicaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;

    /** El paciente consulta su propia historia. */
    @GetMapping("/api/v1/historia-clinica")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<AtencionResponse>> propia() {
        return ResponseEntity.ok(historiaClinicaService.consultarPropia());
    }

    /** El personal médico consulta la historia de un paciente de su EPS. */
    @GetMapping("/api/v1/pacientes/{idPaciente}/historia-clinica")
    @PreAuthorize("hasRole('PERSONAL_MEDICO')")
    public ResponseEntity<List<AtencionResponse>> deUnPaciente(@PathVariable Long idPaciente) {
        return ResponseEntity.ok(historiaClinicaService.consultarDePaciente(idPaciente));
    }
}