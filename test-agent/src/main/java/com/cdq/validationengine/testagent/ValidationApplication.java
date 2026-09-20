package com.cdq.validationengine.testagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValidationApplication {

    private static final int HTTP_PORT = 8080;
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationApplication.class);

    private ValidationApplication() {
    }

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting validation server on port {}", HTTP_PORT);
        var server = new ValidationHttpServer(HTTP_PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        server.start();

        LOGGER.info("Validation Engine API listening on http://localhost:{}", server.port());
    }
}
