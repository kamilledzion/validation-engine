package com.cdq.validationengine.rulecatalog;

public enum RuleField {

    COUNTRY("country"),
    VAT_ID("vatId"),
    IBAN("iban");

    private final String fieldName;

    RuleField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}
