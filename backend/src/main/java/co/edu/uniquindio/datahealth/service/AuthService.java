package co.edu.uniquindio.datahealth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.datahealth.exception.CredencialesInvalidasException;
import co.edu.uniquindio.datahealth.exception.CuentaBloqueadaException;
import co.edu.uniquindio.datahealth.model.dto.LoginRequest;
import co.edu.uniquindio.datahealth.model.dto.LoginResponse;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import co.edu.uniquindio.datahealth.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Hash BCrypt de relleno: se compara cuando el correo no existe, para que la respuesta
    // tarde lo mismo que con un correo real y no delate qué correos están registrados.
    private static final String HASH_RELLENO = "$2a$10$C/0HcKhbaKRezRx5lWzo2ekAc325kph.sLCM4ETtfAFkvmy1RJPMu";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;

    @Value("${app.security.max-failed-attempts}")
    private int maxIntentos;

    @Value("${app.security.lock-minutes}")
    private long minutosBloqueo;

    // noRollbackFor: el contador de intentos y la auditoría deben guardarse aunque el login falle.
    @Transactional(noRollbackFor = { CredencialesInvalidasException.class, CuentaBloqueadaException.class })
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

        // Correo inexistente: mismo mensaje y mismo tiempo que una contraseña incorrecta.
        if (usuario == null) {
            passwordEncoder.matches(request.password(), HASH_RELLENO);
            auditoriaService.registrar(null, email, "LOGIN_FALLIDO", "Correo no registrado");
            throw new CredencialesInvalidasException();
        }

        // Usuario dado de baja: se trata igual que credenciales inválidas.
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            auditoriaService.registrar(usuario.getIdEps(), usuario.getEmail(), "LOGIN_FALLIDO", "Usuario inactivo");
            throw new CredencialesInvalidasException();
        }

        // Regla de negocio: un usuario bloqueado no entra aunque sus credenciales sean correctas.
        if (usuario.estaBloqueado()) {
            auditoriaService.registrar(usuario.getIdEps(), usuario.getEmail(), "LOGIN_BLOQUEADO",
                    "Intento de ingreso con la cuenta bloqueada");
            throw new CuentaBloqueadaException(minutosRestantes(usuario));
        }

        // El bloqueo ya venció: se empieza de nuevo con el contador en cero.
        if (usuario.getBloqueadoHasta() != null) {
            usuario.setBloqueadoHasta(null);
            usuario.setIntentosFallidos(0);
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            registrarFallo(usuario);
        }

        // Éxito: se reinicia el contador (ADR-04).
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(usuario.getIdEps(), usuario.getEmail(), "LOGIN_EXITOSO", null);
        return new LoginResponse(jwtService.generarToken(usuario));
    }

    /** Suma el intento fallido; al llegar al máximo bloquea la cuenta. Siempre lanza una excepción. */
    private void registrarFallo(Usuario usuario) {
        usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

        if (usuario.getIntentosFallidos() >= maxIntentos) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutosBloqueo));
            usuarioRepository.save(usuario);
            // Esta alerta es la que verá el administrador de la entidad (RF-05).
            auditoriaService.registrar(usuario.getIdEps(), usuario.getEmail(), "CUENTA_BLOQUEADA",
                    "Bloqueo por " + maxIntentos + " intentos fallidos consecutivos");
            throw new CuentaBloqueadaException(minutosBloqueo);
        }

        usuarioRepository.save(usuario);
        auditoriaService.registrar(usuario.getIdEps(), usuario.getEmail(), "LOGIN_FALLIDO",
                "Intento " + usuario.getIntentosFallidos() + " de " + maxIntentos);
        throw new CredencialesInvalidasException();
    }

    private long minutosRestantes(Usuario usuario) {
        long segundos = Duration.between(LocalDateTime.now(), usuario.getBloqueadoHasta()).toSeconds();
        return Math.max(1, (long) Math.ceil(segundos / 60.0));
    }
}