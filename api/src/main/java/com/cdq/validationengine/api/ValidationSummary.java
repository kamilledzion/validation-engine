package com.cdq.validationengine.api;

import java.util.HashMap;
import java.util.Map;

public final class ValidationSummary {

    private long resultCount;
    private long errorCount;
    private final Map<Decision, Long> decisions = new HashMap<>();
    private final Map<Severity, Long> severities = new HashMap<>();

    public ValidationSummary() {
        for (Decision decision : Decision.values()) {
            decisions.put(decision, 0L);
        }
        for (Severity severity : Severity.values()) {
            severities.put(severity, 0L);
        }
    }

    public long getErrorCount() {
        return errorCount;
    }

    public long getResultCount() {
        return resultCount;
    }

    public Map<Decision, Long> getDecisions() {
        return decisions;
    }

    public Map<Severity, Long> getSeverities() {
        return severities;
    }

    public void addResult(RuleResult result) {
        resultCount++;
        decisions.merge(result.decision(), 1L, Long::sum);
        severities.merge(result.severity(), 1L, Long::sum);
    }

    public void addError() {
        errorCount++;
    }
}
