package com.cdq.validationengine.core.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RecordContext {

    private final Record record;
    private final Map<String, Object> fieldsRead = new LinkedHashMap<>();

    public RecordContext(Record record) {
        this.record = record;
    }

    public String stringField(String name) {
        if (!fieldsRead.containsKey(name)) {
            fieldsRead.put(name, record.field(name));
        }
        Object field = fieldsRead.get(name);

        if (field == null) {
            return null;
        }

        if (!(field instanceof String text)) {
            throw new IllegalArgumentException("Expected a string for field " + name);
        }

        return text;
    }

    public Map<String, Object> getFieldsRead() {
        return fieldsRead;
    }
}
