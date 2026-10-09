package co.edu.uniquindio.datahealth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.datahealth.model.entity.Usuario;
import co.edu.uniquindio.datahealth.model.enums.Rol;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** El rol y el id_eps que viajan en el JWT son la base del control de acceso (ADR-03, ADR-04). */
class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-con-mas-de-32-caracteres-ok";

    private Usuario usuario(Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(5L);
        u.setIdEps(7L);
        u.setNombre("Ana");
        u.setEmail("ana@eps.com");
        u.setRol(rol);
        return u;
    }

    @Test
    void tokenValidoDevuelveUsuarioConRolYEps() {
        JwtService jwt = new JwtService(SECRETO, 60);

        Optional<UsuarioAutenticado> r = jwt.validar(jwt.generarToken(usuario(Rol.ADMIN_ENTIDAD)));

        assertTrue(r.isPresent());
        assertEquals(Rol.ADMIN_ENTIDAD, r.get().rol());
        assertEquals(7L, r.get().idEps());
        assertEquals(5L, r.get().idUsuario());
        assertEquals("ana@eps.com", r.get().email());
    }

    @Test
    void tokenVencidoSeRechaza() {
        JwtService jwt = new JwtService(SECRETO, -1);

        assertTrue(jwt.validar(jwt.generarToken(usuario(Rol.PACIENTE))).isEmpty());
    }

    @Test
    void tokenManipuladoSeRechaza() {
        JwtService jwt = new JwtService(SECRETO, 60);
        String token = jwt.generarToken(usuario(Rol.PACIENTE));
        int i = token.lastIndexOf('.') + 1;
        char otro = token.charAt(i) == 'A' ? 'B' : 'A';

        assertTrue(jwt.validar(token.substring(0, i) + otro + token.substring(i + 1)).isEmpty());
    }

    @Test
    void tokenFirmadoConOtraClaveSeRechaza() {
        JwtService jwt = new JwtService(SECRETO, 60);
        String ajeno = Jwts.builder()
                .subject("ana@eps.com")
                .signWith(Keys.hmacShaKeyFor("otra-clave-distinta-de-32-caracteres!!".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertTrue(jwt.validar(ajeno).isEmpty());
    }

    @Test
    void textoQueNoEsUnTokenSeRechaza() {
        assertTrue(new JwtService(SECRETO, 60).validar("no-es-un-jwt").isEmpty());
    }

    @Test
    void secretoCortoImpideArrancar() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 60));
    }
}
