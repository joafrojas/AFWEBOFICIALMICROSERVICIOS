package microservicio.foro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForoExternalIntegrationTest {

    // These are integration tests that call a running Foro microservice.
    // They are skipped by default. To enable, run Maven with:
    // mvnw -DrunIntegrationTests=true -Dforo.base.url=http://localhost:8082/foro-api test

    @Test
    public void getPosts_shouldReturn2xx_whenServiceRunning() throws Exception {
        Assumptions.assumeTrue(Boolean.parseBoolean(System.getProperty("runIntegrationTests", "false")),
                "Integration tests are disabled (set -DrunIntegrationTests=true to enable)");

        String base = System.getProperty("foro.base.url", "http://localhost:8082/foro-api");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + "/api/posts"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "Expected 2xx response from Foro service, got: " + response.statusCode());
    }

    @Test
    public void postCreate_shouldReturn2xx_whenServiceRunning() throws Exception {
        Assumptions.assumeTrue(Boolean.parseBoolean(System.getProperty("runIntegrationTests", "false")),
                "Integration tests are disabled (set -DrunIntegrationTests=true to enable)");

        String base = System.getProperty("foro.base.url", "http://localhost:8082/foro-api");

        String payload = "{\"title\":\"Integration Test Post\",\"authorId\":\"itest\",\"category\":\"Test\"}";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + "/api/posts"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "Expected 2xx response from Foro POST, got: " + response.statusCode() + " body: " + response.body());
    }
}
