package e2e;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

abstract class BaseE2E {
    // E2E_BASE_URL=http://localhost:4200 (o la URL de la Static Web App)
    protected static final String BASE = System.getenv().getOrDefault("E2E_BASE_URL", "http://localhost:4200");
    // Clave de los usuarios demo (la que corresponde al hash del script 04)
    protected static final String CLAVE = System.getenv().getOrDefault("E2E_PASSWORD", "CAMBIAR");

    protected WebDriver driver;
    protected WebDriverWait espera;

    @BeforeEach
    void abrir() {
        ChromeOptions o = new ChromeOptions();
        if ("true".equals(System.getenv("E2E_HEADLESS"))) o.addArguments("--headless=new");
        o.addArguments("--window-size=1400,1000");
        driver = new ChromeDriver(o);       // sesión nueva = sessionStorage limpio
        espera = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void cerrar() { if (driver != null) driver.quit(); }

    protected void login(String email, String clave) {
        driver.get(BASE + "/login");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(clave);
        driver.findElement(By.cssSelector("form button[type=submit]")).click();
    }

    protected void loginOk(String email) {
        login(email, CLAVE);
        espera.until(ExpectedConditions.urlContains("/inicio"));
    }

    protected void cerrarSesion() {
        driver.findElement(By.xpath("//button[normalize-space()='Cerrar sesión']")).click();
        espera.until(ExpectedConditions.urlContains("/login"));
    }

    protected String textoError() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".aviso--error"))).getText();
    }

    protected boolean menuTiene(String etiqueta) {
        return !driver.findElements(By.xpath("//nav[@aria-label='Módulos']//a[normalize-space()='" + etiqueta + "']")).isEmpty();
    }

    protected void buscarPaciente(String documento) {
        driver.get(BASE + "/pacientes");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("documento"))).sendKeys(documento);
        driver.findElement(By.cssSelector("form.busqueda button[type=submit]")).click();
    }
}