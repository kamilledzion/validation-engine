package com.cdq.validationengine.jsonadapter;

import com.cdq.validationengine.core.model.Record;
import tools.jackson.databind.JsonNode;

final class JsonRecord implements Record {

    private final JsonNode node;

    public JsonRecord(JsonNode node) {
        this.node = node;
    }

    @Override
    public Object field(String name) {
        JsonNode value = node.get(name);

        if (value == null) {
            return null;
        }

        return switch (value.getNodeType()) {
            case NULL -> null;
            case STRING -> value.stringValue();
            case BOOLEAN -> value.booleanValue();
            case NUMBER -> value.numberValue();
            default -> throw new IllegalArgumentException("Unsupported field type: " + value.getNodeType());
        };
    }
}
