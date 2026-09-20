package com.cdq.validationengine.core;

import com.cdq.validationengine.core.model.Record;
import com.cdq.validationengine.core.model.RecordContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.cdq.validationengine.core.BaseTestDataBuilder.recordFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordContextTest {

    private static final String VAT_ID_FIELD = "vatId";
    private static final String VAT_ID_VALUE = "123";

    @Test
    void shouldReadNullAndEmptyFields() {
        // given
        var emptyFieldName = "empty";
        var nilFieldName = "nil";
        var missingFieldName = "missing";
        var emptyStringValue = "";

        var fields = new HashMap<String, Object>();
        fields.put(emptyFieldName, emptyStringValue);
        fields.put(nilFieldName, null);
        fields.put("unused", "x");
        var context = new RecordContext(recordFrom(fields));

        // when
        var empty = context.stringField(emptyFieldName);
        var nil = context.stringField(nilFieldName);
        var missing = context.stringField(missingFieldName);

        // then
        assertThat(empty).isEmpty();
        assertThat(nil).isNull();
        assertThat(missing).isNull();
        assertThat(context.getFieldsRead())
                .containsEntry(missingFieldName, null)
                .containsEntry(nilFieldName, null)
                .containsEntry(emptyFieldName, emptyStringValue);
    }

    @Test
    void shouldThrowExceptionWhenFieldIsNotString() {
        // given
        var context = new RecordContext(recordFrom(Map.of(VAT_ID_FIELD, 123)));

        // when - then
        assertThatThrownBy(() -> context.stringField(VAT_ID_FIELD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected a string for field vatId");
        assertThat(context.getFieldsRead()).containsOnlyKeys(VAT_ID_FIELD).containsEntry(VAT_ID_FIELD, 123);
    }

    @Test
    void shouldReadFieldOnlyOnceWhenIsRequestedAgain() {
        // given
        var reads = new AtomicInteger();
        Record partner = name -> {
            if (VAT_ID_FIELD.equals(name)) {
                reads.incrementAndGet();
                return VAT_ID_VALUE;
            }
            return null;
        };
        var context = new RecordContext(partner);

        // when
        var firstRead = context.stringField(VAT_ID_FIELD);
        var secondRead = context.stringField(VAT_ID_FIELD);

        // then
        assertThat(firstRead).isEqualTo(VAT_ID_VALUE);
        assertThat(secondRead).isEqualTo(VAT_ID_VALUE);
        assertThat(reads.get()).isEqualTo(1);
        assertThat(context.getFieldsRead()).containsEntry(VAT_ID_FIELD, VAT_ID_VALUE);
    }

    @Test
    void shouldUseFallbackIdWhenRecordIdIsMissing() {
        // given - when - then
        assertThat(recordFrom(Map.of()).id()).isEqualTo("<missing id>");
    }
}
