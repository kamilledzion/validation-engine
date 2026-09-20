package com.cdq.validationengine.rulecatalog.rule;

import com.cdq.validationengine.core.model.RecordContext;
import com.cdq.validationengine.core.model.Rule;
import com.cdq.validationengine.api.RuleValue;

import static com.cdq.validationengine.api.RuleValue.BAD;
import static com.cdq.validationengine.api.RuleValue.OK;
import static com.cdq.validationengine.rulecatalog.RuleField.COUNTRY;
import static com.cdq.validationengine.rulecatalog.RuleField.IBAN;

public final class IbanFormatRule implements Rule {

    public static final String ID = "ibanFormat";

    @Override
    public RuleValue evaluate(RecordContext record) {
        String iban = record.stringField(IBAN.fieldName());
        if (iban == null || iban.isEmpty()) {
            return BAD;
        }

        String country = record.stringField(COUNTRY.fieldName());
        return country != null && iban.startsWith(country)
                ? OK
                : BAD;
    }
}
