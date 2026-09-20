package com.cdq.validationengine.rulecatalog.rule;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.cdq.validationengine.api.RuleValue.BLOCKED;
import static com.cdq.validationengine.api.RuleValue.OK;
import static com.cdq.validationengine.rulecatalog.rule.BaseTestDataBuilder.COUNTRY_FIELD;
import static com.cdq.validationengine.rulecatalog.rule.BaseTestDataBuilder.recordFrom;
import static org.assertj.core.api.Assertions.assertThat;

class CountryBlockedRuleTest {

    private final CountryBlockedRule rule = new CountryBlockedRule();

    @Test
    void shouldReturnBlockedWhenCountryIsZZ() {
        // given
        var record = recordFrom(Map.of(COUNTRY_FIELD, "ZZ"));

        // when
        var value = rule.evaluate(record);

        // then
        assertThat(value).isEqualTo(BLOCKED);
        assertThat(record.getFieldsRead()).containsExactly(Map.entry(COUNTRY_FIELD, "ZZ"));
    }

    @Test
    void shouldReturnOkWhenCountryIsAllowedOrMissing() {
        // given
        var recordAllowed = recordFrom(Map.of(COUNTRY_FIELD, "DE"));
        var recordMissing = recordFrom(Map.of());

        // when
        var valueAllowed = rule.evaluate(recordAllowed);
        var valueMissing = rule.evaluate(recordMissing);

        // when - then
        assertThat(valueAllowed).isEqualTo(OK);
        assertThat(valueMissing).isEqualTo(OK);
    }
}
