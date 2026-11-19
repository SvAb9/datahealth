package e2e;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.*;

@DisplayName("Flujos E2E · reglas de negocio")
class ReglasDeNegocioE2E extends BaseE2E {

    @Test @DisplayName("TC-01 Login válido muestra el menú del rol")
    void loginValidoMuestraMenuDelRol() {
        loginOk("admin@demo.com");
        assertTrue(menuTiene("Usuarios"));
        assertTrue(menuTiene("Auditoría"));
        assertFalse(menuTiene("Pacientes"));
    }

    @Test @DisplayName("TC-10 RF-04: el médico no entra a /usuarios")
    void medicoNoEntraAUsuarios() {
        loginOk("medico@demo.com");
        assertFalse(menuTiene("Usuarios"));
        driver.get(BASE + "/usuarios");
        espera.until(ExpectedConditions.urlContains("/inicio"));
    }

    @Test @DisplayName("TC-03 Correo inexistente da mensaje genérico")
    void correoInexistenteMensajeGenerico() {
        login("nadie@demo.com", "Cualquiera123");
        assertTrue(textoError().contains("Correo o contraseña incorrectos"));
    }

    @Test @DisplayName("TC-06 RN-01: paciente de otra EPS se ve como inexistente")
    void aislamientoPorEps() {
        loginOk("recepcion@demo.com");

        buscarPaciente("10203040"); // EPS Demo
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".aviso--ok")));

        driver.get(BASE + "/pacientes");
        buscarPaciente("50607080"); // pertenece a Salud Vida EPS
        WebElementAviso.esperarInfo(espera, "No hay un paciente con ese documento");
    }

    @Test @DisplayName("TC-06b RN-01: la otra EPS sí ve a su paciente")
    void otraEpsVeSuPaciente() {
        loginOk("recepcion@saludvida.com");
        buscarPaciente("50607080");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".aviso--ok")));
    }

    @Test @DisplayName("TC-07 Autorregistro con documento existente se rechaza")
    void autorregistroConDocumentoExistente() {
        driver.get(BASE + "/registro");
        espera.until(d -> d.findElements(By.cssSelector("#idEps option")).size() > 1);
        new Select(driver.findElement(By.id("idEps"))).selectByVisibleText("EPS Demo");
        driver.findElement(By.id("documento")).sendKeys("10203040");
        driver.findElement(By.id("edad")).sendKeys("70");
        driver.findElement(By.id("nombre")).sendKeys("Persona De Prueba");
        driver.findElement(By.id("telefono")).sendKeys("3001234567");
        driver.findElement(By.id("email")).sendKeys("qa" + System.currentTimeMillis() + "@correo.com");
        driver.findElement(By.id("password")).sendKeys("Clave12345");
        driver.findElement(By.cssSelector("form button[type=submit]")).click();
        assertTrue(textoError().contains("No pudimos crear tu cuenta"));
    }

    @Test @DisplayName("TC-02/04 Admin crea usuario; 3 intentos fallidos lo bloquean y queda en auditoría")
    void bloqueoTrasTresIntentos() {
        String email = "qa" + System.currentTimeMillis() + "@demo.com";

        // 1) el admin registra un médico
        loginOk("admin@demo.com");
        driver.get(BASE + "/usuarios");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombre"))).sendKeys("Medico QA");
        driver.findElement(By.id("email")).sendKeys(email);
        new Select(driver.findElement(By.id("rol"))).selectByValue("PERSONAL_MEDICO");
        driver.findElement(By.id("password")).sendKeys("Clave12345");
        driver.findElement(By.cssSelector("form button[type=submit]")).click();
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".aviso--ok")));
        cerrarSesion();

        // 2) tres claves malas
        for (int i = 0; i < 3; i++) {
            login(email, "ClaveMala" + i);
            espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".aviso--error")));
            driver.navigate().refresh();
        }
        login(email, "ClaveMala3");
        assertTrue(textoError().toLowerCase().contains("bloqueada"));

        // 3) ni con la clave correcta entra
        driver.navigate().refresh();
        login(email, "Clave12345");
        assertTrue(textoError().toLowerCase().contains("bloqueada"));

        // 4) el admin ve la alerta
        driver.get(BASE + "/login");
        login("admin@demo.com", CLAVE);
        espera.until(ExpectedConditions.urlContains("/inicio"));
        driver.get(BASE + "/auditoria");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("tr.alerta")));
    }
}

final class WebElementAviso {
    static void esperarInfo(WebDriverWait espera, String texto) {
        espera.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".aviso--info"), texto));
    }
}