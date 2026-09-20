package com.cdq.validationengine.rulecatalog.rule;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.cdq.validationengine.api.RuleValue.BAD;
import static com.cdq.validationengine.api.RuleValue.OK;
import static com.cdq.validationengine.rulecatalog.rule.BaseTestDataBuilder.COUNTRY_FIELD;
import static com.cdq.validationengine.rulecatalog.rule.BaseTestDataBuilder.recordFrom;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class IbanFormatRuleTest {

    private static final String IBAN_FIELD = "iban";
    private final IbanFormatRule rule = new IbanFormatRule();

    @Test
    void shouldReturnOkWhenIbanStartsWithCountry() {
        // given
        var record = recordFrom(Map.of(IBAN_FIELD, "DE123456789", COUNTRY_FIELD, "DE"));

        // when
        var value = rule.evaluate(record);

        // then
        assertThat(value).isEqualTo(OK);
        assertThat(record.getFieldsRead())
                .containsExactly(
                        entry(IBAN_FIELD, "DE123456789"),
                        entry("country", "DE"));
    }

    @Test
    void shouldReturnBadWhenIbanIsEmptyOrMissing() {
        // given
        var recordEmptyIban = recordFrom(Map.of(IBAN_FIELD, "", COUNTRY_FIELD, "DE"));
        var recordMissingIban = recordFrom(Map.of(COUNTRY_FIELD, "DE"));

        // when
        var valueEmptyIban = rule.evaluate(recordEmptyIban);
        var valueMissingIban = rule.evaluate(recordMissingIban);

        // then
        assertThat(valueEmptyIban).isEqualTo(BAD);
        assertThat(valueMissingIban).isEqualTo(BAD);
        assertThat(recordEmptyIban.getFieldsRead()).containsOnlyKeys(IBAN_FIELD);
        assertThat(recordMissingIban.getFieldsRead()).containsOnlyKeys(IBAN_FIELD);
    }

    @Test
    void shouldReturnBadWhenCountryIsMissingOrNotMatchIban() {
        // given
        var recordMissingCountry = recordFrom(Map.of(IBAN_FIELD, "DE123456789"));
        var recordDifferentCountry = recordFrom(Map.of(IBAN_FIELD, "DE123456789", "country", "FR"));

        // when
        var valueMissingCountry = rule.evaluate(recordMissingCountry);
        var valueDifferentCountry = rule.evaluate(recordDifferentCountry);

        // then
        assertThat(valueMissingCountry).isEqualTo(BAD);
        assertThat(valueDifferentCountry).isEqualTo(BAD);
    }
}
