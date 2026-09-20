package com.cdq.validationengine.jsonadapter;

import com.cdq.validationengine.rulecatalog.RuleCatalog;
import com.cdq.validationengine.core.model.RuleMapping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

final class JsonRuleCatalogReader {

    private static final String DEFAULT_RULE_CATALOG_FILE = "rule-catalog.json";
    private static final ObjectReader READER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()
            .readerForListOf(RuleMapping.class);

    public List<RuleMapping> defaults() throws IOException {
        try (var input = RuleCatalog.class.getResourceAsStream(DEFAULT_RULE_CATALOG_FILE)) {
            if (input == null) {
                throw new IOException(String.format("Default file %s was not found", DEFAULT_RULE_CATALOG_FILE));
            }
            return read(input);
        }
    }

    public List<RuleMapping> read(InputStream input) {
        return READER.readValue(input);
    }
}
