package com.cdq.validationengine.core;

import com.cdq.validationengine.api.RuleError;
import com.cdq.validationengine.api.RuleResult;
import com.cdq.validationengine.api.RuleValue;
import com.cdq.validationengine.api.Decision;
import com.cdq.validationengine.api.Severity;
import com.cdq.validationengine.core.model.Rule;
import com.cdq.validationengine.core.model.RuleMapping;
import com.cdq.validationengine.core.model.Record;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cdq.validationengine.core.BaseTestDataBuilder.recordFrom;
import static com.cdq.validationengine.api.Decision.*;
import static com.cdq.validationengine.api.RuleValue.BAD;
import static com.cdq.validationengine.api.RuleValue.OK;
import static com.cdq.validationengine.api.Severity.ERROR;
import static com.cdq.validationengine.core.model.Status.RELEASED;
import static org.assertj.core.api.Assertions.*;

class ValidationEngineTest {

    private static final String VAT_ID_FIELD_NAME = "vatId";
    private static final String VAT_RULE_ID = "vat";
    private static final String RECORD_ID_1 = "r1";
    private static final String RECORD_ID_2 = "r2";
    private static final String HEALTHY_RULE_ID = "healthy";
    private static final String ID_FIELD_NAME = "id";

    @Test
    void shouldReturnSummaryWithResultsAndErrorsWhenOneRuleFails() {
        // given
        var brokenRuleId = "broken";
        var exceptionMsg = "broken logic";
        List<RuleResult> results = new ArrayList<>();
        List<RuleError> errors = new ArrayList<>();

        var records = List.of(
                recordFrom(Map.of(ID_FIELD_NAME, RECORD_ID_1, VAT_ID_FIELD_NAME, "123")),
                recordFrom(Map.of(ID_FIELD_NAME, RECORD_ID_2)));
        var mapping = List.of(mapping(brokenRuleId), mapping(VAT_RULE_ID));
        Map<String, Rule> rules = Map.of(
                brokenRuleId, _ -> {
                    throw new IllegalArgumentException(exceptionMsg);
                },
                VAT_RULE_ID, context -> context.stringField(VAT_ID_FIELD_NAME) == null ? BAD : OK);

        var engine = new ValidationEngine(mapping, rules);

        // when
        var summary = engine.validate(records, results::add, errors::add);

        // then
        assertThat(results).extracting(RuleResult::decision).containsExactly(VALID, INVALID);
        assertThat(results).extracting(RuleResult::recordId).containsExactly(RECORD_ID_1, RECORD_ID_2);
        assertThat(results.getFirst().fieldsRead().get(VAT_ID_FIELD_NAME)).isEqualTo("123");
        assertThat(results.getLast().fieldsRead()).containsEntry(VAT_ID_FIELD_NAME, null);
        assertThat(errors).extracting(RuleError::ruleId).containsExactly(brokenRuleId, brokenRuleId);
        assertThat(errors).extracting(RuleError::recordId).containsExactly(RECORD_ID_1, RECORD_ID_2);
        assertThat(errors).extracting(RuleError::message).containsExactly(exceptionMsg, exceptionMsg);
        assertThat(summary.getResultCount()).isEqualTo(2);
        assertThat(summary.getErrorCount()).isEqualTo(2);
        assertThat(summary.getDecisions()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        VALID, 1L, INVALID, 1L,
                        REVIEW, 0L, Decision.NOT_APPLICABLE, 0L));
        assertThat(summary.getSeverities()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        ERROR, 2L, Severity.WARNING, 0L, Severity.INFO, 0L));
    }

    @Test
    void shouldReturnDefaultDecisionWhenRuleValueIsNotMapped() {
        // given
        var results = new ArrayList<RuleResult>();
        var errors = new ArrayList<RuleError>();
        var records = List.of(recordFrom(Map.of(ID_FIELD_NAME, RECORD_ID_1)), recordFrom(Map.of(ID_FIELD_NAME, RECORD_ID_2)));
        var mapping = List.of(mapping(VAT_RULE_ID));
        Map<String, Rule> rules = Map.of(VAT_RULE_ID, _ -> RuleValue.BLOCKED);
        var engine = new ValidationEngine(mapping, rules);

        // when
        engine.validate(records, results::add, errors::add);

        // then
        assertThat(results).extracting(RuleResult::recordId).containsExactly(RECORD_ID_1, RECORD_ID_2);
        assertThat(results).extracting(RuleResult::decision).containsExactly(REVIEW, REVIEW);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldContinueValidationWhenRecordReadFails() {
        // given
        var exceptionMsg = "Cannot read field ";
        var results = new ArrayList<RuleResult>();
        var errors = new ArrayList<RuleError>();

        var mapping = List.of(mapping(VAT_RULE_ID), mapping(HEALTHY_RULE_ID));
        Map<String, Rule> rules = Map.of(
                VAT_RULE_ID, context -> context.stringField(VAT_ID_FIELD_NAME) == null ? BAD : OK,
                HEALTHY_RULE_ID, _ -> OK);
        var engine = new ValidationEngine(mapping, rules);

        Record recordBroken = name -> {
            if (name.equals(ID_FIELD_NAME)) {
                return RECORD_ID_1;
            }
            throw new IllegalStateException(exceptionMsg + name);
        };
        var records = List.of(recordBroken, recordFrom(Map.of(ID_FIELD_NAME, RECORD_ID_2)));

        // when
        engine.validate(records, results::add, errors::add);

        // then
        assertThat(results).extracting(RuleResult::recordId, RuleResult::ruleId, RuleResult::decision)
                .containsExactlyInAnyOrder(
                        tuple(RECORD_ID_1, HEALTHY_RULE_ID, VALID),
                        tuple(RECORD_ID_2, HEALTHY_RULE_ID, VALID),
                        tuple(RECORD_ID_2, VAT_RULE_ID, INVALID));
        assertThat(errors).singleElement().satisfies(error -> {
            assertThat(error.recordId()).isEqualTo(RECORD_ID_1);
            assertThat(error.ruleId()).isEqualTo(VAT_RULE_ID);
            assertThat(error.errorType()).isEqualTo(IllegalStateException.class.getSimpleName());
            assertThat(error.message()).isEqualTo(exceptionMsg + VAT_ID_FIELD_NAME);
        });
    }

    @Test
    void shouldThrowExceptionWhenRuleIsMissing() {
        // given - when - then
        assertThatNullPointerException().isThrownBy(() ->
                new ValidationEngine(List.of(mapping(VAT_RULE_ID)), Map.of()));
    }

    private RuleMapping mapping(String id) {
        return new RuleMapping(id, "Label for " + id, RELEASED, ERROR, "WORLD", List.of(), Map.of(OK, VALID, BAD, INVALID), REVIEW);
    }
}
