package com.cdq.validationengine.jsonadapter;

import com.cdq.validationengine.api.RuleResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.cdq.validationengine.api.Decision.*;
import static com.cdq.validationengine.api.Severity.*;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class JsonValidatorTest {

    @Test
    void shouldValidateJsonAgainstDefaultRules() throws Exception {
        // given
        var json = """
                [
                  {
                    "id": "r1",
                    "vatId": "DE111111111",
                    "country": "DE",
                    "iban": "DE123456789"
                  },
                  {
                    "id": "r2",
                    "vatId": "FR22",
                    "country": "FR",
                    "iban": "FR123456789"
                  },
                  {
                    "id": "r3",
                    "country": "ZZ",
                    "iban": ""
                  }
                ]
                """;
        var input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        var results = new ArrayList<RuleResult>();
        var errors = new ArrayList<>();

        // when
        var summary = new JsonValidator().validate(input, results::add, errors::add);

        // then
        assertThat(errors).isEmpty();
        assertThat(results).hasSize(9);
        assertThat(summary.getResultCount()).isEqualTo(9);
        assertThat(summary.getErrorCount()).isZero();
        assertThat(summary.getDecisions()).containsExactlyInAnyOrderEntriesOf(
                of(
                        VALID, 3L,
                        INVALID, 4L,
                        REVIEW, 0L,
                        NOT_APPLICABLE, 2L));
        assertThat(summary.getSeverities()).containsExactlyInAnyOrderEntriesOf(
                of(
                        ERROR, 9L,
                        WARNING, 0L,
                        INFO, 0L));
        assertThat(results.stream().filter(result -> result.recordId().equals("r1")))
                .extracting(RuleResult::ruleId, RuleResult::decision)
                .containsExactlyInAnyOrder(
                        tuple("countryBlocked", NOT_APPLICABLE),
                        tuple("vatFormat", VALID),
                        tuple("ibanFormat", VALID));
        assertThat(results.stream().filter(result -> result.recordId().equals("r2")))
                .extracting(RuleResult::ruleId, RuleResult::decision)
                .containsExactlyInAnyOrder(
                        tuple("countryBlocked", NOT_APPLICABLE),
                        tuple("vatFormat", INVALID),
                        tuple("ibanFormat", VALID));
        assertThat(results.stream().filter(result -> result.recordId().equals("r3")))
                .extracting(RuleResult::decision)
                .containsExactlyInAnyOrder(INVALID, INVALID, INVALID);
    }
}
