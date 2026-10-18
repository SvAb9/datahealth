package co.edu.uniquindio.datahealth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.datahealth.exception.CredencialesInvalidasException;
import co.edu.uniquindio.datahealth.exception.CuentaBloqueadaException;
import co.edu.uniquindio.datahealth.model.dto.LoginRequest;
import co.edu.uniquindio.datahealth.model.dto.LoginResponse;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.JwtService;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;

/** Pruebas unitarias de HU-01 y HU-02 (login, bloqueo y logout). No necesitan Oracle. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuditoriaService auditoriaService;

    AuthService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AuthService(usuarioRepository, passwordEncoder, jwtService, auditoriaService);
        ReflectionTestUtils.setField(servicio, "maxIntentos", 3);
        ReflectionTestUtils.setField(servicio, "minutosBloqueo", 15L);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private Usuario usuario(int intentos) {
        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setIdEps(7L);
        u.setNombre("Ana");
        u.setEmail("ana@eps.com");
        u.setPasswordHash("HASH");
        u.setRol(Rol.PERSONAL_MEDICO);
        u.setActivo(true);
        u.setIntentosFallidos(intentos);
        return u;
    }

    @Test
    void loginCorrectoDevuelveTokenYReiniciaIntentos() {
        Usuario u = usuario(2);
        when(usuarioRepository.findByEmailIgnoreCase("ana@eps.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Clave1234", "HASH")).thenReturn(true);
        when(jwtService.generarToken(u)).thenReturn("TOKEN");

        LoginResponse r = servicio.login(new LoginRequest(" Ana@EPS.com ", "Clave1234"));

        assertEquals("TOKEN", r.token());
        assertEquals(0, u.getIntentosFallidos());
        verify(auditoriaService).registrar(7L, "ana@eps.com", "LOGIN_EXITOSO", null);
    }

    @Test
    void correoInexistenteResponde401GenericoYSeAudita() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@eps.com")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.login(new LoginRequest("nadie@eps.com", "x")));
        verify(auditoriaService).registrar(null, "nadie@eps.com", "LOGIN_FALLIDO", "Correo no registrado");
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void claveIncorrectaSumaUnIntento() {
        Usuario u = usuario(0);
        when(usuarioRepository.findByEmailIgnoreCase("ana@eps.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.login(new LoginRequest("ana@eps.com", "mala")));
        assertEquals(1, u.getIntentosFallidos());
        assertNull(u.getBloqueadoHasta());
    }

    @Test
    void elTercerIntentoFallidoBloqueaLaCuenta() {
        Usuario u = usuario(2);
        when(usuarioRepository.findByEmailIgnoreCase("ana@eps.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        assertThrows(CuentaBloqueadaException.class,
                () -> servicio.login(new LoginRequest("ana@eps.com", "mala")));
        assertTrue(u.estaBloqueado());
        verify(auditoriaService).registrar(eq(7L), eq("ana@eps.com"), eq("CUENTA_BLOQUEADA"), anyString());
    }

    @Test
    void cuentaBloqueadaNoEntraNiConClaveCorrecta() {
        Usuario u = usuario(3);
        u.setBloqueadoHasta(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByEmailIgnoreCase("ana@eps.com")).thenReturn(Optional.of(u));

        assertThrows(CuentaBloqueadaException.class,
                () -> servicio.login(new LoginRequest("ana@eps.com", "Clave1234")));
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void usuarioInactivoSeTrataComoCredencialesInvalidas() {
        Usuario u = usuario(0);
        u.setActivo(false);
        when(usuarioRepository.findByEmailIgnoreCase("ana@eps.com")).thenReturn(Optional.of(u));

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.login(new LoginRequest("ana@eps.com", "Clave1234")));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void logoutDejaConstanciaEnLaAuditoria() {
        var autenticado = new UsuarioAutenticado(1L, "ana@eps.com", "Ana", Rol.PERSONAL_MEDICO, 7L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(autenticado, null, List.of()));

        servicio.logout();

        verify(auditoriaService).registrar(7L, "ana@eps.com", "LOGOUT", null);
    }
}
