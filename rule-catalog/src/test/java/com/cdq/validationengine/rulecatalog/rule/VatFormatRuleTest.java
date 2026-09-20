package com.cdq.validationengine.rulecatalog.rule;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.cdq.validationengine.api.RuleValue.BAD;
import static com.cdq.validationengine.api.RuleValue.OK;
import static com.cdq.validationengine.rulecatalog.rule.BaseTestDataBuilder.VAT_ID_FIELD;
import static com.cdq.validationengine.rulecatalog.rule.BaseTestDataBuilder.recordFrom;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class VatFormatRuleTest {

    private final VatFormatRule rule = new VatFormatRule();

    @Test
    void shouldReturnOkWhenVatIdLengthIs9() {
        // given
        var record = recordFrom(Map.of(VAT_ID_FIELD, "DE111111111"));

        // when
        var value = rule.evaluate(record);

        // then
        assertThat(value).isEqualTo(OK);
        assertThat(record.getFieldsRead()).containsExactly(entry(VAT_ID_FIELD, "DE111111111"));
    }

    @Test
    void shouldReturnBadWhenVatIdIsTooShortOrMissing() {
        // given
        var recordTooShort = recordFrom(Map.of(VAT_ID_FIELD, "FR22"));
        var recordMissing = recordFrom(Map.of());

        var valueTooShort = rule.evaluate(recordTooShort);
        var valueMissing = rule.evaluate(recordMissing);

        // when - then
        assertThat(valueTooShort).isEqualTo(BAD);
        assertThat(valueMissing).isEqualTo(BAD);
    }
}
