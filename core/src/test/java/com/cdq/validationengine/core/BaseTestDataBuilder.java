package com.cdq.validationengine.core;

import com.cdq.validationengine.core.model.Record;

import java.util.Map;

class BaseTestDataBuilder {

    static Record recordFrom(Map<String, Object> fields) {
        return fields::get;
    }
}
