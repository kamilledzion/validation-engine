package com.cdq.validationengine.core.model;

public interface Record {

    Object field(String name);

    default String id() {
        Object value = field("id");

        return value == null
                ? "<missing id>"
                : value.toString();
    }
}
