package com.cdq.validationengine.engine;

import com.cdq.validationengine.api.Validator;
import com.cdq.validationengine.jsonadapter.JsonValidator;

import java.io.IOException;

public final class Validators {

    private Validators() {}

    public static Validator createDefault() throws IOException {
        return new JsonValidator();
    }
}
