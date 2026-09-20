package com.cdq.validationengine.testagent;

import com.cdq.validationengine.api.RuleError;
import com.cdq.validationengine.api.RuleResult;
import com.cdq.validationengine.api.ValidationSummary;
import com.cdq.validationengine.engine.Validators;
import com.cdq.validationengine.api.Validator;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ValidationHttpServer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationHttpServer.class);
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final ObjectWriter RESULTS = MAPPER.writerFor(RuleResult.class);
    private static final ObjectWriter ERRORS = MAPPER.writerFor(RuleError.class);
    private static final ObjectWriter SUMMARY = MAPPER.writerFor(ValidationSummary.class);

    private static final String RULE_RESULTS_JSON = "rule-results.json";
    private static final String RULE_ERRORS_JSON = "rule-errors.json";
    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String VALIDATION_ID = "Validation-Id";
    private static final String ID = "id";

    private final Javalin server;
    private final Validator validation;
    private final Path outputDirectory;

    public ValidationHttpServer(int port) throws IOException {
        this(port, Validators.createDefault(), Path.of("validation-output"));
    }

    private ValidationHttpServer(int port, Validator validation, Path outputDirectory) throws IOException {
        this.validation = validation;
        this.outputDirectory = Files.createDirectories(outputDirectory.toAbsolutePath());
        this.server = Javalin.create(config -> {
            config.jetty.host = "localhost";
            config.jetty.port = port;
            config.routes.get("/health", ctx -> ctx.result("OK"));
            config.routes.post("/validate", this::validate);
            config.routes.get("/validation/{" + ID + "}/results", ctx -> download(ctx, RULE_RESULTS_JSON));
            config.routes.get("/validation/{" + ID + "}/errors", ctx -> download(ctx, RULE_ERRORS_JSON));
        });
    }

    public void start() {
        server.start();
    }

    public int port() {
        return server.port();
    }

    private void validate(Context ctx) throws IOException {
        InputStream input = ctx.bodyInputStream();
        Path directory = Files.createTempDirectory(outputDirectory, "");

        ValidationSummary summary;
        try (var results = RESULTS.writeValuesAsArray(directory.resolve(RULE_RESULTS_JSON).toFile());
             var errors = ERRORS.writeValuesAsArray(directory.resolve(RULE_ERRORS_JSON).toFile())) {
            summary = validation.validate(input, results::write, errors::write);
        }
        Files.createFile(directory.resolve("completed"));

        String validationId = directory.getFileName().toString();
        LOGGER.info("Validation id: {} completed: {} results, {} errors", validationId, summary.getResultCount(), summary.getErrorCount());

        ctx.header(VALIDATION_ID, validationId);
        ctx.contentType(CONTENT_TYPE);
        ctx.result(SUMMARY.writeValueAsString(summary));
    }

    private void download(Context ctx, String filename) throws IOException {
        String id = ctx.pathParam(ID);
        Path path = outputDirectory.resolve(id);
        Path file = path.resolve(filename);

        LOGGER.debug("Retrieving {} for validation {}", filename, id);

        ctx.contentType(CONTENT_TYPE);
        ctx.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        try (var input = Files.newInputStream(file)) {
            input.transferTo(ctx.res().getOutputStream());
        }
    }

    @Override
    public void close() {
        LOGGER.info("Stopping validation server");
        server.stop();
    }
}
