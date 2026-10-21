package co.edu.uniquindio.datahealth.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.uniquindio.datahealth.config.CorsConfig;
import co.edu.uniquindio.datahealth.controller.AuditoriaController;
import co.edu.uniquindio.datahealth.exception.GlobalExceptionHandler;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.service.AuditoriaService;

/** Solo el administrador de entidad consulta la auditoría (RF-04). */
@WebMvcTest(AuditoriaController.class)
@Import({ SecurityConfig.class, JwtService.class, CorsConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-prueba-con-mas-de-32-caracteres-ok",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origin=http://localhost:4200" })
class SeguridadAuditoriaTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean AuditoriaService auditoriaService;

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
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/v1/auditoria")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "PERSONAL_MEDICO", "PERSONAL_ADMINISTRATIVO", "PACIENTE" })
    void otrosRolesNoVenLaAuditoria(Rol rol) throws Exception {
        mvc.perform(get("/api/v1/auditoria").header("Authorization", bearer(rol)))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorDeEntidadVeLaAuditoria() throws Exception {
        mvc.perform(get("/api/v1/auditoria").header("Authorization", bearer(Rol.ADMIN_ENTIDAD)))
                .andExpect(status().isOk());
    }
}