package co.edu.uniquindio.datahealth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.datahealth.model.dto.PacienteResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarPacienteRequest;
import co.edu.uniquindio.datahealth.service.PacienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /** 404 si el paciente no existe en la EPS del usuario. */
    @GetMapping
    @PreAuthorize("hasAnyRole('PERSONAL_MEDICO', 'PERSONAL_ADMINISTRATIVO')")
    public ResponseEntity<PacienteResponse> buscar(@RequestParam String documento) {
        return ResponseEntity.ok(pacienteService.buscarPorDocumento(documento));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PERSONAL_MEDICO', 'PERSONAL_ADMINISTRATIVO')")
    public ResponseEntity<PacienteResponse> registrar(@Valid @RequestBody RegistrarPacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.registrar(request));
    }
}