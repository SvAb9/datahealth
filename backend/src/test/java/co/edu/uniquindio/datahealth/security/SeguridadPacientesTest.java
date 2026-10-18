package co.edu.uniquindio.datahealth.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.uniquindio.datahealth.config.CorsConfig;
import co.edu.uniquindio.datahealth.controller.HistoriaClinicaController;
import co.edu.uniquindio.datahealth.controller.PacienteController;
import co.edu.uniquindio.datahealth.exception.GlobalExceptionHandler;
import co.edu.uniquindio.datahealth.exception.PacienteNoEncontradoException;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.service.HistoriaClinicaService;
import co.edu.uniquindio.datahealth.service.PacienteService;

/** Comprueba por HTTP que cada rol solo entra a pacientes e historia clínica según le corresponde (RF-04). */
@WebMvcTest({ PacienteController.class, HistoriaClinicaController.class })
@Import({ SecurityConfig.class, JwtService.class, CorsConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-prueba-con-mas-de-32-caracteres-ok",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origin=http://localhost:4200" })
class SeguridadPacientesTest {

    private static final String NUEVO =
            "{\"documento\":\"10203040\",\"nombre\":\"Ana Perez\",\"edad\":70,\"telefono\":\"3101234567\"}";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean PacienteService pacienteService;
    @MockitoBean HistoriaClinicaService historiaClinicaService;

    private String bearer(Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setIdEps(7L);
        u.setNombre("Prueba");
        u.setEmail("prueba@eps.com");
        u.setRol(rol);
        return "Bearer " + jwtService.generarToken(u);
    }

    @Test
    void buscarPacienteSinTokenResponde401() throws Exception {
        mvc.perform(get("/api/v1/pacientes").param("documento", "10203040"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "PERSONAL_MEDICO", "PERSONAL_ADMINISTRATIVO" })
    void personalMedicoYAdministrativoPuedenBuscarPacientes(Rol rol) throws Exception {
        mvc.perform(get("/api/v1/pacientes").param("documento", "10203040").header("Authorization", bearer(rol)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "ADMIN_ENTIDAD", "PACIENTE" })
    void otrosRolesNoPuedenBuscarPacientes(Rol rol) throws Exception {
        mvc.perform(get("/api/v1/pacientes").param("documento", "10203040").header("Authorization", bearer(rol)))
                .andExpect(status().isForbidden());
    }

    @Test
    void pacienteInexistenteResponde404() throws Exception {
        when(pacienteService.buscarPorDocumento("10203040")).thenThrow(new PacienteNoEncontradoException());

        mvc.perform(get("/api/v1/pacientes").param("documento", "10203040")
                        .header("Authorization", bearer(Rol.PERSONAL_ADMINISTRATIVO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarPacienteConPersonalAdministrativoResponde201() throws Exception {
        mvc.perform(post("/api/v1/pacientes").header("Authorization", bearer(Rol.PERSONAL_ADMINISTRATIVO))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "ADMIN_ENTIDAD", "PACIENTE" })
    void otrosRolesNoPuedenRegistrarPacientes(Rol rol) throws Exception {
        mvc.perform(post("/api/v1/pacientes").header("Authorization", bearer(rol))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarConDocumentoInvalidoResponde400() throws Exception {
        mvc.perform(post("/api/v1/pacientes").header("Authorization", bearer(Rol.PERSONAL_MEDICO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"12ab\",\"nombre\":\"Ana Perez\",\"edad\":70,\"telefono\":\"3101234567\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elPacienteVeSuPropiaHistoria() throws Exception {
        mvc.perform(get("/api/v1/historia-clinica").header("Authorization", bearer(Rol.PACIENTE)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "PERSONAL_MEDICO", "PERSONAL_ADMINISTRATIVO", "ADMIN_ENTIDAD" })
    void soloElPacienteUsaLaHistoriaPropia(Rol rol) throws Exception {
        mvc.perform(get("/api/v1/historia-clinica").header("Authorization", bearer(rol)))
                .andExpect(status().isForbidden());
    }

    @Test
    void elMedicoVeLaHistoriaDeUnPaciente() throws Exception {
        mvc.perform(get("/api/v1/pacientes/5/historia-clinica").header("Authorization", bearer(Rol.PERSONAL_MEDICO)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "PERSONAL_ADMINISTRATIVO", "ADMIN_ENTIDAD", "PACIENTE" })
    void sinSerMedicoNoSeVeLaHistoriaDeOtroPaciente(Rol rol) throws Exception {
        mvc.perform(get("/api/v1/pacientes/5/historia-clinica").header("Authorization", bearer(rol)))
                .andExpect(status().isForbidden());
    }
}