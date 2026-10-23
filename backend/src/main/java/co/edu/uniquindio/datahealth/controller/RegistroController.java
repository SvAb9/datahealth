package co.edu.uniquindio.datahealth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.datahealth.model.dto.EpsResponse;
import co.edu.uniquindio.datahealth.model.dto.LoginResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistroPacienteRequest;
import co.edu.uniquindio.datahealth.service.RegistroPacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Endpoints públicos (sin JWT) del autorregistro de pacientes. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroPacienteService registroPacienteService;

    @GetMapping("/eps")
    public ResponseEntity<List<EpsResponse>> entidades() {
        return ResponseEntity.ok(registroPacienteService.listarEntidades());
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroPacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroPacienteService.registrar(request));
    }
}