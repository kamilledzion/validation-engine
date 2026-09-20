package com.cdq.validationengine.api;

import java.util.Map;

public record RuleResult(String recordId,
                         String ruleId,
                         Severity severity,
                         RuleValue computedValue,
                         Decision decision,
                         Map<String, Object> fieldsRead) {
}
