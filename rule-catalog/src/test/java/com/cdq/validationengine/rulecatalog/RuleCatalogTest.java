package com.cdq.validationengine.rulecatalog;

import com.cdq.validationengine.rulecatalog.rule.CountryBlockedRule;
import com.cdq.validationengine.rulecatalog.rule.IbanFormatRule;
import com.cdq.validationengine.rulecatalog.rule.VatFormatRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleCatalogTest {

    @Test
    void shouldReturnDefaultRules() {
        // given - when
        var rules = RuleCatalog.defaults();

        // then
        assertThat(rules).containsOnlyKeys(CountryBlockedRule.ID, VatFormatRule.ID, IbanFormatRule.ID);
        assertThat(rules.get(CountryBlockedRule.ID)).isInstanceOf(CountryBlockedRule.class);
        assertThat(rules.get(VatFormatRule.ID)).isInstanceOf(VatFormatRule.class);
        assertThat(rules.get(IbanFormatRule.ID)).isInstanceOf(IbanFormatRule.class);
    }
}
