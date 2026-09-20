package com.cdq.validationengine.rulecatalog;

import com.cdq.validationengine.rulecatalog.rule.CountryBlockedRule;
import com.cdq.validationengine.rulecatalog.rule.IbanFormatRule;
import com.cdq.validationengine.core.model.Rule;
import com.cdq.validationengine.rulecatalog.rule.VatFormatRule;

import java.util.Map;

public final class RuleCatalog {

    private RuleCatalog() {}

    public static Map<String, Rule> defaults() {
        return Map.of(
                CountryBlockedRule.ID, new CountryBlockedRule(),
                VatFormatRule.ID, new VatFormatRule(),
                IbanFormatRule.ID, new IbanFormatRule());
    }
}
