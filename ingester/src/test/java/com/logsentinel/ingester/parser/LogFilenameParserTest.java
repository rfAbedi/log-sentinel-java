package com.logsentinel.ingester.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogFilenameParserTest {

    private final LogFilenameParser parser = new LogFilenameParser();

    @Test
    void extractsHyphenatedComponentFromValidFilename() {
        String component = parser.extractComponent("payment-service_2025-07-01_12-55-55.log");

        assertEquals("payment-service", component);
    }

    @Test
    void preservesDotsAndUnderscoresInComponent() {
        String component = parser.extractComponent("inventory.api_v2_2024-02-29_23-59-59.log");

        assertEquals("inventory.api_v2", component);
    }
}
