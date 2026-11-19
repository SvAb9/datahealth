package co.edu.uniquindio.datahealth.security;

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
import co.edu.uniquindio.datahealth.controller.OrdenController;
import co.edu.uniquindio.datahealth.exception.GlobalExceptionHandler;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.service.OrdenService;

/** Solo el personal médico registra órdenes y resultados (RF-04). */
@WebMvcTest(OrdenController.class)
@Import({ SecurityConfig.class, JwtService.class, CorsConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-prueba-con-mas-de-32-caracteres-ok",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origin=http://localhost:4200" })
class SeguridadOrdenesTest {

    private static final String ORDEN = "{\"tipo\":\"MEDICA\",\"descripcion\":\"Control en 3 meses\"}";
    private static final String RESULTADO = "{\"resultado\":\"Glucosa 98 mg/dL\"}";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean OrdenService ordenService;

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
        mvc.perform(post("/api/v1/citas/1/ordenes").contentType(MediaType.APPLICATION_JSON).content(ORDEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elMedicoPuedeAgregarOrdenesYResultados() throws Exception {
        mvc.perform(post("/api/v1/citas/1/ordenes").header("Authorization", bearer(Rol.PERSONAL_MEDICO))
                        .contentType(MediaType.APPLICATION_JSON).content(ORDEN))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/ordenes/1/resultados").header("Authorization", bearer(Rol.PERSONAL_MEDICO))
                        .contentType(MediaType.APPLICATION_JSON).content(RESULTADO))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "PERSONAL_ADMINISTRATIVO", "ADMIN_ENTIDAD", "PACIENTE" })
    void otrosRolesNoPuedenEscribir(Rol rol) throws Exception {
        mvc.perform(post("/api/v1/citas/1/ordenes").header("Authorization", bearer(rol))
                        .contentType(MediaType.APPLICATION_JSON).content(ORDEN))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/ordenes/1/resultados").header("Authorization", bearer(rol))
                        .contentType(MediaType.APPLICATION_JSON).content(RESULTADO))
                .andExpect(status().isForbidden());
    }
}