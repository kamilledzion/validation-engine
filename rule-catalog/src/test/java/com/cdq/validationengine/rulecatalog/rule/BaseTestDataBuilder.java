package com.cdq.validationengine.rulecatalog.rule;

import com.cdq.validationengine.core.model.RecordContext;

import java.util.Map;

class BaseTestDataBuilder {

    static final String COUNTRY_FIELD = "country";
    static final String VAT_ID_FIELD = "vatId";

    static RecordContext recordFrom(Map<String, Object> fields) {
        return new RecordContext(fields::get);
    }
}
