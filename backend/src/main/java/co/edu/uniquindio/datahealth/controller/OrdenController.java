package co.edu.uniquindio.datahealth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.datahealth.model.dto.OrdenResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistrarOrdenRequest;
import co.edu.uniquindio.datahealth.model.dto.RegistrarResultadoRequest;
import co.edu.uniquindio.datahealth.model.dto.ResultadoResponse;
import co.edu.uniquindio.datahealth.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    /** 409 si la atención está cerrada; 404 si no existe en la EPS del médico. */
    @PostMapping("/api/v1/citas/{idCita}/ordenes")
    @PreAuthorize("hasRole('PERSONAL_MEDICO')")
    public ResponseEntity<OrdenResponse> agregarOrden(@PathVariable Long idCita,
                                                      @Valid @RequestBody RegistrarOrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.agregarOrden(idCita, request));
    }

    @PostMapping("/api/v1/ordenes/{idOrden}/resultados")
    @PreAuthorize("hasRole('PERSONAL_MEDICO')")
    public ResponseEntity<ResultadoResponse> registrarResultado(@PathVariable Long idOrden,
                                                                @Valid @RequestBody RegistrarResultadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.registrarResultado(idOrden, request));
    }
}