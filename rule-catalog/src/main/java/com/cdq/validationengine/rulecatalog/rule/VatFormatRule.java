package com.cdq.validationengine.rulecatalog.rule;

import com.cdq.validationengine.core.model.RecordContext;
import com.cdq.validationengine.core.model.Rule;
import com.cdq.validationengine.api.RuleValue;

import static com.cdq.validationengine.api.RuleValue.BAD;
import static com.cdq.validationengine.api.RuleValue.OK;
import static com.cdq.validationengine.rulecatalog.RuleField.VAT_ID;

public final class VatFormatRule implements Rule {
    public static final String ID = "vatFormat";

    @Override
    public RuleValue evaluate(RecordContext record) {
        String vatId = record.stringField(VAT_ID.fieldName());

        return vatId != null && vatId.length() >= 9
                ? OK
                : BAD;
    }
}
