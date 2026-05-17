package org.example.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NoteAcceptanceTest {

    private RemoteWebDriver driver;
    private WebDriverWait wait;
    private HttpClient httpClient;

    private String appUrl;
    private String apiBase;

    /**
     * Armazena o ID da nota criada em cada teste para limpeza no tearDown.
     */
    private Long createdNoteId;

    @BeforeAll
    void setup() throws Exception {
        String seleniumHub = System.getProperty("selenium.hub",
                "http://selenium-chrome:4444/wd/hub");
        appUrl = System.getProperty("app.url", "http://nginx-dev");
        apiBase = System.getProperty("api.base", appUrl + "/api");

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        driver = new RemoteWebDriver(new URL(seleniumHub), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Timeout implícito curto — usamos wait explícito para sincronização
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));
    }

    @AfterAll
    void cleanup() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Remove todas as notas do banco antes de cada teste, garantindo
     * isolamento contra dados residuais de execuções anteriores.
     */
    @BeforeEach
    void cleanDatabase() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(apiBase + "/notes/"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        while (matcher.find()) {
            long id = Long.parseLong(matcher.group(1));
            httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiBase + "/notes/" + id))
                            .DELETE()
                            .build(),
                    HttpResponse.BodyHandlers.discarding());
        }
    }

    /**
     * Remove a nota criada durante o teste para manter o banco limpo.
     */
    @AfterEach
    void tearDown() throws Exception {
        if (createdNoteId != null) {
            httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiBase + "/notes/" + createdNoteId))
                            .DELETE()
                            .build(),
                    HttpResponse.BodyHandlers.discarding());
            createdNoteId = null;
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    /**
     * Cria uma nota via chamada REST direta e retorna o ID gerado.
     */
    private long createNoteViaApi(String title, String content) throws Exception {
        String json = String.format("""
                {"title":"%s","content":"%s"}
                """, escapeJson(title), escapeJson(content));

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(apiBase + "/notes/"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(),
                "Deveria criar nota via API com 201 Created");

        String body = response.body();
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        assertTrue(matcher.find(), "Resposta da API deveria conter 'id'");
        return Long.parseLong(matcher.group(1));
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Espera até que o texto esteja presente no elemento identificado.
     */
    private void waitForText(By locator, String expectedText) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    // ------------------------------------------------------------------ //
    //  Testes — Listagem de Notas
    // ------------------------------------------------------------------ //

    @Test
    void testListPage_showsEmptyState() {
        driver.get(appUrl + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("notes")));

        // Título da página
        String pageTitle = driver.findElement(By.cssSelector(".page-title")).getText();
        assertEquals("Notas.com", pageTitle);

        // Link "Criar nota" deve estar visível
        WebElement criarLink = driver.findElement(By.linkText("Criar nota"));
        assertTrue(criarLink.isDisplayed());
        assertTrue(criarLink.getAttribute("href").endsWith("/new.html"),
                "Link 'Criar nota' deveria apontar para new.html");

        // Estado vazio
        String notesText = driver.findElement(By.id("notes")).getText();
        assertTrue(notesText.contains("Sem notas."),
                "Deveria exibir 'Sem notas.' quando não há notas");
    }

    @Test
    void testListPage_showsExistingNotes() throws Exception {
        // Cria uma nota via API para que ela apareça na lista
        createdNoteId = createNoteViaApi("Nota da lista", "Conteúdo visível na index");

        // Navega para a página inicial
        driver.get(appUrl + "/");
        waitForText(By.id("notes"), "Nota da lista");

        WebElement notesContainer = driver.findElement(By.id("notes"));

        // Verifica que o título aparece como link para a página de detalhes
        assertTrue(notesContainer.getText().contains("Nota da lista"));
        WebElement titleLink = notesContainer.findElement(
                By.cssSelector("a[href$='/show.html?id=" + createdNoteId + "']"));
        assertTrue(titleLink.isDisplayed(),
                "Título deveria ser link para show.html com o ID da nota");

        // Verifica que existe link "Editar" para a nota
        WebElement editLink = notesContainer.findElement(
                By.xpath(".//a[contains(@href, 'update.html?id=" + createdNoteId + "')]"));
        assertTrue(editLink.isDisplayed());
        assertTrue(editLink.getAttribute("href").contains("update.html?id=" + createdNoteId),
                "Link Editar deveria apontar para update.html com o ID da nota");

        // Verifica que existe botão "Deletar" para a nota
        WebElement deleteBtn = notesContainer.findElement(
                By.cssSelector(".delete-btn[data-id='" + createdNoteId + "']"));
        assertTrue(deleteBtn.isDisplayed());
    }

    // ------------------------------------------------------------------ //
    //  Testes — Detalhes da Nota (Show)
    // ------------------------------------------------------------------ //

    @Test
    void testShowPage_displaysExistingNoteData() throws Exception {
        createdNoteId = createNoteViaApi("Título do show", "Conteúdo da página de detalhes");

        // Navega para a página de detalhes
        driver.get(appUrl + "/show.html?id=" + createdNoteId);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        // Aguarda o título ser carregado dinamicamente
        waitForText(By.id("title"), "Título do show");

        // Verifica que o título e conteúdo são exibidos
        assertEquals("Título do show", driver.findElement(By.id("title")).getText());
        assertEquals("Conteúdo da página de detalhes",
                driver.findElement(By.id("content")).getText());

        // Verifica que os botões de ação estão presentes
        WebElement updateBtn = driver.findElement(By.id("update-btn"));
        WebElement deleteBtn = driver.findElement(By.id("delete-btn"));
        assertTrue(updateBtn.isDisplayed(), "Botão Editar deveria estar visível");
        assertTrue(deleteBtn.isDisplayed(), "Botão Deletar deveria estar visível");

        // Verifica que o link "Voltar" está presente
        WebElement backLink = driver.findElement(By.cssSelector(".back-link"));
        assertTrue(backLink.isDisplayed());
        assertTrue(backLink.getAttribute("href").endsWith("/"),
                "Link Voltar deveria apontar para a raiz");
    }

    @Test
    void testShowPage_deleteNote_redirectsToIndex() throws Exception {
        createdNoteId = createNoteViaApi("Nota para deletar", "Será removida pelo show");

        // Navega para a página de detalhes
        driver.get(appUrl + "/show.html?id=" + createdNoteId);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        // Aguarda o título carregar e clica em Deletar
        waitForText(By.id("title"), "Nota para deletar");
        driver.findElement(By.id("delete-btn")).click();

        // Aguarda o redirect para a página inicial
        wait.until(ExpectedConditions.urlMatches(".*/$"));

        // Verifica que a nota não aparece mais na lista
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("notes")));
        String notesText = driver.findElement(By.id("notes")).getText();
        assertFalse(notesText.contains("Nota para deletar"),
                "Nota deletada não deveria aparecer na lista");

        // Nota já foi removida, evita que o tearDown tente deletar novamente
        createdNoteId = null;
    }

    @Test
    void testShowPage_updateButton_navigatesToUpdatePage() throws Exception {
        createdNoteId = createNoteViaApi("Nota via show", "Conteúdo via show");

        // Navega para a página de detalhes
        driver.get(appUrl + "/show.html?id=" + createdNoteId);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
        waitForText(By.id("title"), "Nota via show");

        // Clica no botão Editar
        driver.findElement(By.id("update-btn")).click();

        // Aguarda navegação para a página de update
        wait.until(ExpectedConditions.urlContains("update.html?id=" + createdNoteId));

        // Verifica que a página de update carregou os dados da nota
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
        WebElement titleInput = driver.findElement(By.id("title"));
        wait.until(ExpectedConditions.not(
                ExpectedConditions.attributeToBe(titleInput, "value", "")));
        assertEquals("Nota via show", titleInput.getAttribute("value"));
    }

    @Test
    void testShowPage_invalidId_redirectsToIndex() {
        // Navega para show.html com ID inválido
        driver.get(appUrl + "/show.html?id=abc");

        // Deve redirecionar imediatamente para a raiz
        wait.until(ExpectedConditions.urlMatches(".*/$"));

        // Verifica que caiu na página inicial
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("notes")));
    }

    // ------------------------------------------------------------------ //
    //  Testes — Criação de Notas
    // ------------------------------------------------------------------ //

    @Test
    void testCreateNote_createsAndRedirectsToList() {
        driver.get(appUrl + "/new.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        // Verifica o título da página (h2.page-title, pois o h1 é "Notas.com")
        assertEquals("Nova nota", driver.findElement(By.cssSelector("h2.page-title")).getText());

        // Preenche o formulário
        driver.findElement(By.id("title")).sendKeys("Nota de aceitação");
        driver.findElement(By.id("content")).sendKeys("Conteúdo gerado pelo teste de aceitação");

        // Clica em Salvar
        driver.findElement(By.id("save-btn")).click();

        // Aguarda o redirect para a página inicial
        // Porta 80 é default HTTP, o browser pode omiti-la na URL
        wait.until(ExpectedConditions.urlMatches(".*/$"));

        // Verifica que a nota aparece na lista
        waitForText(By.id("notes"), "Nota de aceitação");

        // Extrai o ID da nota para limpeza — olha no link "Editar"
        WebElement editLink = driver.findElement(By.partialLinkText("Editar"));
        String href = editLink.getAttribute("href");
        Matcher matcher = Pattern.compile("id=(\\d+)").matcher(href);
        if (matcher.find()) {
            createdNoteId = Long.parseLong(matcher.group(1));
        }
    }

    // ------------------------------------------------------------------ //
    //  Testes — Atualização de Notas
    // ------------------------------------------------------------------ //

    @Test
    void testUpdatePage_loadsExistingData() throws Exception {
        // Cria uma nota via API para editar
        createdNoteId = createNoteViaApi("Título original", "Conteúdo original");

        // Navega para a página de edição
        driver.get(appUrl + "/update.html?id=" + createdNoteId);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        // Verifica o título da página
        assertEquals("Editar nota", driver.findElement(By.cssSelector(".page-title")).getText());

        // Verifica que os dados foram carregados no formulário
        WebElement titleInput = driver.findElement(By.id("title"));
        WebElement contentInput = driver.findElement(By.id("content"));

        wait.until(ExpectedConditions.not(
                ExpectedConditions.attributeToBe(titleInput, "value", "")));
        assertEquals("Título original", titleInput.getAttribute("value"));
        assertEquals("Conteúdo original", contentInput.getAttribute("value"));

        // Verifica que os botões existem
        assertNotNull(driver.findElement(By.id("update-btn")));
        assertNotNull(driver.findElement(By.id("delete-btn")));
    }

    @Test
    void testUpdateNote_updatesAndRedirectsToList() throws Exception {
        // Cria nota via API
        createdNoteId = createNoteViaApi("Antes da edição", "Conteúdo antes");

        // Navega para a edição
        driver.get(appUrl + "/update.html?id=" + createdNoteId);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        // Aguarda o carregamento dos dados
        WebElement titleInput = driver.findElement(By.id("title"));
        wait.until(ExpectedConditions
                .attributeToBe(titleInput, "value", "Antes da edição"));

        // Limpa e preenche com novos valores
        titleInput.clear();
        titleInput.sendKeys("Depois da edição");
        WebElement contentInput = driver.findElement(By.id("content"));
        contentInput.clear();
        contentInput.sendKeys("Conteúdo atualizado pelo teste");

        // Clica em Atualizar
        driver.findElement(By.id("update-btn")).click();

        // Aguarda o redirect para a página inicial
        // Porta 80 é default HTTP, o browser pode omiti-la na URL
        wait.until(ExpectedConditions.urlMatches(".*/$"));

        // Verifica que a nota atualizada aparece na lista
        waitForText(By.id("notes"), "Depois da edição");
    }
}
