package com.cdq.validationengine.rulecatalog.rule;

import com.cdq.validationengine.core.model.RecordContext;
import com.cdq.validationengine.core.model.Rule;
import com.cdq.validationengine.api.RuleValue;

import static com.cdq.validationengine.api.RuleValue.BLOCKED;
import static com.cdq.validationengine.api.RuleValue.OK;
import static com.cdq.validationengine.rulecatalog.RuleField.COUNTRY;

public final class CountryBlockedRule implements Rule {

    public static final String ID = "countryBlocked";

    @Override
    public RuleValue evaluate(RecordContext record) {
        return "ZZ".equals(record.stringField(COUNTRY.fieldName()))
                ? BLOCKED
                : OK;
    }
}
