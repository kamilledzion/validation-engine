package com.cdq.validationengine.core;

import com.cdq.validationengine.api.RuleError;
import com.cdq.validationengine.api.RuleResult;
import com.cdq.validationengine.api.ValidationSummary;
import com.cdq.validationengine.core.model.Record;
import com.cdq.validationengine.core.model.RecordContext;
import com.cdq.validationengine.core.model.Rule;
import com.cdq.validationengine.core.model.RuleMapping;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public final class ValidationEngine {

    private final Map<RuleMapping, Rule> rulesByMapping;

    public ValidationEngine(List<RuleMapping> ruleMappings, Map<String, Rule> rulesById) {
        this.rulesByMapping = ruleMappings.stream()
                .collect(toMap(
                        Function.identity(),
                        mapping -> rulesById.get(mapping.id())));
    }

    public ValidationSummary validate(Iterable<? extends Record> records, Consumer<RuleResult> results, Consumer<RuleError> errors) {
        var summary = new ValidationSummary();
        records.forEach(record -> validateRecord(record, results, errors, summary));
        return summary;
    }

    private void validateRecord(Record record, Consumer<RuleResult> results, Consumer<RuleError> errors, ValidationSummary summary) {
        String recordId = record.id();

        for (RuleMapping mapping : rulesByMapping.keySet()) {
            var recordContext = new RecordContext(record);
            RuleResult ruleResult;
            try {
                var ruleValue = rulesByMapping.get(mapping).evaluate(recordContext);
                var decision = mapping.decisions().getOrDefault(ruleValue, mapping.defaultDecision());
                ruleResult = new RuleResult(recordId, mapping.id(), mapping.severity(), ruleValue, decision, recordContext.getFieldsRead());
            } catch (RuntimeException e) {
                summary.addError();
                errors.accept(new RuleError(recordId, mapping.id(), e.getClass().getSimpleName(), e.getMessage(), recordContext.getFieldsRead()));
                continue;
            }
            summary.addResult(ruleResult);
            results.accept(ruleResult);
        }
    }
}
