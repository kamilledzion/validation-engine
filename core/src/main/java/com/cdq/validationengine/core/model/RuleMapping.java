package com.cdq.validationengine.core.model;

import com.cdq.validationengine.api.Decision;
import com.cdq.validationengine.api.RuleValue;
import com.cdq.validationengine.api.Severity;

import java.util.List;
import java.util.Map;

public record RuleMapping(
        String id,
        String label,
        Status status,
        Severity severity,
        String countryScope,
        List<Category> categories,
        Map<RuleValue, Decision> decisions,
        Decision defaultDecision
) {
}
