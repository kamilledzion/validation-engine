package com.cdq.validationengine.api;

import java.util.Map;

public record RuleError(String recordId,
                        String ruleId,
                        String errorType,
                        String message,
                        Map<String, Object> fieldsRead) {
}
