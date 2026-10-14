package co.edu.uniquindio.datahealth.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import co.edu.uniquindio.datahealth.controller.AuthController;
import co.edu.uniquindio.datahealth.controller.UsuarioController;
import co.edu.uniquindio.datahealth.exception.GlobalExceptionHandler;
import co.edu.uniquindio.datahealth.model.dto.LoginResponse;
import co.edu.uniquindio.datahealth.model.dto.UsuarioResponse;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.service.AuthService;
import co.edu.uniquindio.datahealth.service.UsuarioService;

/** Comprueba por HTTP que cada rol solo entra a lo que le corresponde (RF-04, ADR-04). */
@WebMvcTest({ UsuarioController.class, AuthController.class })
@Import({ SecurityConfig.class, JwtService.class, CorsConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-prueba-con-mas-de-32-caracteres-ok",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origin=http://localhost:4200" })
class SeguridadRolesTest {

    private static final String REGISTRO =
            "{\"nombre\":\"Ana Medica\",\"email\":\"ana@eps.com\",\"password\":\"Clave12345\",\"rol\":\"PERSONAL_MEDICO\"}";
    private static final String LOGIN = "{\"email\":\"ana@eps.com\",\"password\":\"Clave12345\"}";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean AuthService authService;

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
    void registrarUsuarioSinTokenResponde401() throws Exception {
        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarUsuarioConTokenInvalidoResponde401() throws Exception {
        mvc.perform(post("/api/v1/usuarios").header("Authorization", "Bearer basura")
                        .contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = { "PERSONAL_MEDICO", "PERSONAL_ADMINISTRATIVO", "PACIENTE" })
    void soloElAdministradorPuedeRegistrarUsuarios(Rol rol) throws Exception {
        mvc.perform(post("/api/v1/usuarios").header("Authorization", bearer(rol))
                        .contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isForbidden());
        verify(usuarioService, never()).registrar(any());
    }

    @Test
    void elAdministradorDeEntidadPuedeRegistrarUsuarios() throws Exception {
        when(usuarioService.registrar(any()))
                .thenReturn(new UsuarioResponse(10L, "Ana Medica", "ana@eps.com", Rol.PERSONAL_MEDICO, true));

        mvc.perform(post("/api/v1/usuarios").header("Authorization", bearer(Rol.ADMIN_ENTIDAD))
                        .contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isCreated());
    }

    @Test
    void elLoginEsPublico() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("TOKEN"));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk());
    }

    @Test
    void logoutSinTokenResponde401() throws Exception {
        mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(Rol.class)
    void logoutLoPuedeUsarCualquierRol(Rol rol) throws Exception {
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", bearer(rol)))
                .andExpect(status().isNoContent());
    }
}
