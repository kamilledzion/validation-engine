package com.cdq.validationengine.api;

import java.io.InputStream;
import java.util.function.Consumer;

public interface Validator {

    ValidationSummary validate(InputStream input, Consumer<RuleResult> results, Consumer<RuleError> errors);
}
