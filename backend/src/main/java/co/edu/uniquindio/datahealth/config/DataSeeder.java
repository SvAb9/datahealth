package co.edu.uniquindio.datahealth.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.datahealth.model.entity.Eps;
import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import co.edu.uniquindio.datahealth.repository.EpsRepository;
import co.edu.uniquindio.datahealth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

/** Solo desarrollo: crea la EPS y el administrador de prueba si aún no existen. */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final EpsRepository epsRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email}")
    private String adminEmail;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        String correo = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.findByEmailIgnoreCase(correo).isPresent()) {
            return;
        }

        Eps eps = epsRepository.findAll().stream().findFirst().orElseGet(() -> {
            Eps nueva = new Eps();
            nueva.setNombre("EPS Demo");
            nueva.setActiva(true);
            return epsRepository.save(nueva);
        });

        Usuario admin = new Usuario();
        admin.setIdEps(eps.getIdEps());
        admin.setNombre("Administrador Demo");
        admin.setEmail(correo);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRol(Rol.ADMIN_ENTIDAD);
        admin.setActivo(true);
        usuarioRepository.save(admin);
    }
}