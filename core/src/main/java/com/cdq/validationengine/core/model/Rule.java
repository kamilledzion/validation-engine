package com.cdq.validationengine.core.model;

import com.cdq.validationengine.api.RuleValue;

public interface Rule {

    RuleValue evaluate(RecordContext record);
}
