package com.cdq.validationengine.jsonadapter;

import com.cdq.validationengine.api.RuleError;
import com.cdq.validationengine.api.RuleResult;
import com.cdq.validationengine.api.ValidationSummary;
import com.cdq.validationengine.api.Validator;
import com.cdq.validationengine.core.ValidationEngine;
import com.cdq.validationengine.core.model.Rule;
import com.cdq.validationengine.core.model.RuleMapping;
import com.cdq.validationengine.rulecatalog.RuleCatalog;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class JsonValidator implements Validator {

    private final JsonRecordReader records;
    private final ValidationEngine engine;

    public JsonValidator() throws IOException {
        this(new JsonRuleCatalogReader().defaults(), RuleCatalog.defaults());
    }

    JsonValidator(List<RuleMapping> ruleMappings, Map<String, Rule> rules) {
        this.records = new JsonRecordReader();
        this.engine = new ValidationEngine(ruleMappings, rules);
    }

    @Override
    public ValidationSummary validate(InputStream input, Consumer<RuleResult> results, Consumer<RuleError> errors) {
        try (var records = this.records.read(input)) {
            return validate(records, results, errors);
        }
    }

    private ValidationSummary validate(Stream<JsonNode> input, Consumer<RuleResult> results, Consumer<RuleError> errors) {
        Iterable<JsonRecord> records = () -> input.map(JsonRecord::new).iterator();
        return engine.validate(records, results, errors);
    }
}
