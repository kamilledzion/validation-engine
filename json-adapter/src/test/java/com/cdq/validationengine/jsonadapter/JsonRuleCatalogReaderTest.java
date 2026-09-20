package com.cdq.validationengine.jsonadapter;

import com.cdq.validationengine.core.model.Category;
import com.cdq.validationengine.core.model.RuleMapping;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.cdq.validationengine.api.Decision.INVALID;
import static com.cdq.validationengine.api.Decision.VALID;
import static com.cdq.validationengine.api.RuleValue.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonRuleCatalogReaderTest {

    private final JsonRuleCatalogReader reader = new JsonRuleCatalogReader();

    @Test
    void shouldLoadDefaultRuleMapping() throws Exception {
        // given - when
        var rules = reader.defaults();

        // then
        assertThat(rules).extracting(RuleMapping::id).containsExactly("countryBlocked", "vatFormat", "ibanFormat");
        assertThat(rules.get(0).decisions()).containsEntry(BLOCKED, INVALID);
        assertThat(rules.get(1).decisions()).containsEntry(OK, VALID).containsEntry(BAD, INVALID);
        assertThat(rules.get(2).decisions()).containsEntry(OK, VALID).containsEntry(BAD, INVALID);
        assertThat(rules).allSatisfy(rule -> {
            assertThat(rule.countryScope()).isEqualTo("WORLD");
            assertThat(rule.categories()).containsExactly(Category.FORMAT);
        });
    }

    @Test
    void shouldThrowExceptionWhenDecisionIsUnknown() {
        // given
        String json = """
                [
                  {
                    "id": "countryBlocked",
                    "label": "Country is allowed",
                    "status": "RELEASED",
                    "severity": "ERROR",
                    "countryScope": "WORLD",
                    "categories": [
                      "FORMAT"
                    ],
                    "decisions": {
                      "BLOCKED": "UNKNOWN"
                    },
                    "defaultDecision": "NOT_APPLICABLE"
                  }
                ]
                """;

        // when - then
        assertThatThrownBy(() -> reader.read(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(Exception.class);
    }
}
