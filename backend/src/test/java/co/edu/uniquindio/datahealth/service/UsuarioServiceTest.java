package co.edu.uniquindio.datahealth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.uniquindio.datahealth.exception.EmailDuplicadoException;
import co.edu.uniquindio.datahealth.exception.RolNoPermitidoException;
import co.edu.uniquindio.datahealth.mapper.UsuarioMapper;
import co.edu.uniquindio.datahealth.model.dto.RegistrarUsuarioRequest;
import co.edu.uniquindio.datahealth.model.dto.UsuarioResponse;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.UsuarioAutenticado;
import co.edu.uniquindio.datahealth.service.AuditoriaService;
import co.edu.uniquindio.datahealth.service.UsuarioService;

/** Prueba unitaria de HU-03: no necesita Oracle (RNF-20). */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final long EPS_ADMIN = 7L;

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditoriaService auditoriaService;

    UsuarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new UsuarioService(usuarioRepository, passwordEncoder, auditoriaService, new UsuarioMapper());
        var admin = new UsuarioAutenticado(1L, "admin@eps.com", "Admin", Rol.ADMIN_ENTIDAD, EPS_ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraUsuarioEnLaEpsDelAdministradorConClaveCifrada() {
        when(usuarioRepository.findByEmailIgnoreCase("medico@eps.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Clave12345")).thenReturn("HASH");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse r = servicio.registrar(
                new RegistrarUsuarioRequest("  Ana Médica ", " Medico@EPS.com ", "Clave12345", Rol.PERSONAL_MEDICO));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(guardado.capture());
        assertEquals(EPS_ADMIN, guardado.getValue().getIdEps());
        assertEquals("HASH", guardado.getValue().getPasswordHash());
        assertEquals("medico@eps.com", guardado.getValue().getEmail());
        assertEquals(1L, guardado.getValue().getCreadoPor());
        assertEquals("Ana Médica", r.nombre());
        assertEquals(Rol.PERSONAL_MEDICO, r.rol());
        verify(auditoriaService).registrar(any(), any(), any(), any());
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarioRepository.findByEmailIgnoreCase("medico@eps.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(EmailDuplicadoException.class, () -> servicio.registrar(
                new RegistrarUsuarioRequest("Ana Médica", "medico@eps.com", "Clave12345", Rol.PERSONAL_MEDICO)));
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void noPermiteCrearOtroAdministrador() {
        assertThrows(RolNoPermitidoException.class, () -> servicio.registrar(
                new RegistrarUsuarioRequest("Otro Admin", "otro@eps.com", "Clave12345", Rol.ADMIN_ENTIDAD)));
        verify(usuarioRepository, never()).saveAndFlush(any());
    }
}
