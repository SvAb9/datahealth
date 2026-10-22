package co.edu.uniquindio.datahealth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.uniquindio.datahealth.exception.DatosInvalidosException;
import co.edu.uniquindio.datahealth.exception.EmailDuplicadoException;
import co.edu.uniquindio.datahealth.exception.PacienteDuplicadoException;
import co.edu.uniquindio.datahealth.model.dto.LoginResponse;
import co.edu.uniquindio.datahealth.model.dto.RegistroPacienteRequest;
import co.edu.uniquindio.datahealth.model.entity.Eps;
import co.edu.uniquindio.datahealth.model.entity.Paciente;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.EpsRepository;
import co.edu.uniquindio.datahealth.repository.PacienteRepository;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.JwtService;

/** Autorregistro de pacientes: siempre rol PACIENTE y EPS elegida; no vincula expedientes ajenos. */
@ExtendWith(MockitoExtension.class)
class RegistroPacienteServiceTest {

    @Mock EpsRepository epsRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuditoriaService auditoriaService;

    RegistroPacienteService servicio;

    @BeforeEach
    void preparar() {
        servicio = new RegistroPacienteService(epsRepository, usuarioRepository, pacienteRepository,
                passwordEncoder, jwtService, auditoriaService);
    }

    private Eps eps() {
        Eps e = new Eps();
        e.setIdEps(7L);
        e.setNombre("EPS Demo");
        return e;
    }

    private RegistroPacienteRequest solicitud() {
        return new RegistroPacienteRequest(7L, "10203040", "  Ana Perez ", 70, " 3101234567 ", " ",
                " Ana@Correo.com ", "Clave12345");
    }

    @Test
    void creaUsuarioPacienteYExpedienteEnLaEpsElegida() {
        when(epsRepository.findById(7L)).thenReturn(Optional.of(eps()));
        when(usuarioRepository.findByEmailIgnoreCase("ana@correo.com")).thenReturn(Optional.empty());
        when(pacienteRepository.existsByIdEpsAndDocumento(7L, "10203040")).thenReturn(false);
        when(passwordEncoder.encode("Clave12345")).thenReturn("HASH");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setIdUsuario(99L);
            return u;
        });
        when(pacienteRepository.saveAndFlush(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("TOKEN");

        LoginResponse r = servicio.registrar(solicitud());

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        ArgumentCaptor<Paciente> paciente = ArgumentCaptor.forClass(Paciente.class);
        verify(usuarioRepository).saveAndFlush(usuario.capture());
        verify(pacienteRepository).saveAndFlush(paciente.capture());

        assertEquals("TOKEN", r.token());
        assertEquals(Rol.PACIENTE, usuario.getValue().getRol());
        assertEquals(7L, usuario.getValue().getIdEps());
        assertEquals("ana@correo.com", usuario.getValue().getEmail());
        assertEquals("HASH", usuario.getValue().getPasswordHash());
        assertEquals(99L, paciente.getValue().getIdUsuario());
        assertEquals("Ana Perez", paciente.getValue().getNombre());
        assertEquals(null, paciente.getValue().getDireccion());
        verify(auditoriaService).registrar(eq(7L), eq("ana@correo.com"), eq("PACIENTE_AUTOREGISTRADO"), anyString());
    }

    @Test
    void rechazaCorreoYaRegistrado() {
        when(epsRepository.findById(7L)).thenReturn(Optional.of(eps()));
        when(usuarioRepository.findByEmailIgnoreCase("ana@correo.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(EmailDuplicadoException.class, () -> servicio.registrar(solicitud()));
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void noVinculaUnExpedienteQueYaExiste() {
        when(epsRepository.findById(7L)).thenReturn(Optional.of(eps()));
        when(usuarioRepository.findByEmailIgnoreCase("ana@correo.com")).thenReturn(Optional.empty());
        when(pacienteRepository.existsByIdEpsAndDocumento(7L, "10203040")).thenReturn(true);

        assertThrows(PacienteDuplicadoException.class, () -> servicio.registrar(solicitud()));
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void rechazaEpsInexistente() {
        when(epsRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(DatosInvalidosException.class, () -> servicio.registrar(solicitud()));
        verifyNoInteractions(usuarioRepository, pacienteRepository);
    }
}