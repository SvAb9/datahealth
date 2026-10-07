package co.edu.uniquindio.datahealth.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

/**
 * El login lo resuelve AuthService; este bean existe para que Spring Security no cree
 * su usuario por defecto con una contraseña aleatoria en consola.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .authorities("ROLE_" + usuario.getRol().name())
                .accountLocked(usuario.estaBloqueado())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}