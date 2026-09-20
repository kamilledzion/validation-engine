package com.cdq.validationengine.testagent;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

import static java.net.URI.create;
import static java.net.http.HttpRequest.newBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class ValidationHttpServerTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String VALIDATION_ID = "Validation-Id";
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final Path PATH = Path.of("validation-output");

    @Test
    void shouldReturnSummaryAndSaveResultsAndErrors() throws Exception {
        // given
        var records = ValidationHttpServerTest.class.getResourceAsStream("/records.json");

        // when
        try (var server = new ValidationHttpServer(0);
             var client = HttpClient.newHttpClient()) {
            server.start();

            var response = post(client, server, records);

            // then
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().allValues(VALIDATION_ID)).isNotEmpty();

            var summary = MAPPER.readTree(response.body());
            assertThat(summary.get("resultCount").longValue()).isEqualTo(9);
            assertThat(summary.get("errorCount").longValue()).isEqualTo(3);
            assertThat(summary.at("/decisions/VALID").longValue()).isEqualTo(3);
            assertThat(summary.at("/decisions/INVALID").longValue()).isEqualTo(4);
            assertThat(summary.at("/decisions/NOT_APPLICABLE").longValue()).isEqualTo(2);
            assertThat(summary.at("/severities/ERROR").longValue()).isEqualTo(9);

            Path outputPath = PATH.resolve(response.headers().firstValue(VALIDATION_ID).orElseThrow());

            var ruleResults = MAPPER.readTree(outputPath.resolve("rule-results.json").toFile());
            var expectedRuleResults = MAPPER.readTree(ValidationHttpServerTest.class.getResourceAsStream("/expected-results.json"));
            assertThat(ruleResults).containsExactlyInAnyOrderElementsOf(expectedRuleResults);

            var ruleErrors = MAPPER.readTree(outputPath.resolve("rule-errors.json").toFile());
            var expectedRuleErrors = MAPPER.readTree(ValidationHttpServerTest.class.getResourceAsStream("/expected-errors.json"));
            assertThat(ruleErrors).containsExactlyInAnyOrderElementsOf(expectedRuleErrors);
        }
    }

    private static HttpResponse<String> post(HttpClient client, ValidationHttpServer server, InputStream inputStream) throws Exception {
        var body = inputStream.readAllBytes();
        var request = request(server, "/validate")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static Builder request(ValidationHttpServer server, String path) {
        return newBuilder(create("http://localhost:" + server.port() + path)).timeout(Duration.ofSeconds(10));
    }
}
