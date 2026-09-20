package com.cdq.validationengine.jsonadapter;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonRecordReaderTest {

    private final JsonRecordReader reader = new JsonRecordReader();

    @Test
    void shouldThrownExceptionWhenSecondRecordIsMalformed() {
        // given
        var input = new ByteArrayInputStream("""
                [
                  {
                    "id": "r1",
                    "count": 3,
                    "active": true,
                    "value": null
                  },
                  {
                    "id": "r1",
                    "broken
                  }
                ]
                """.getBytes(StandardCharsets.UTF_8));

        // when
        try (var records = reader.read(input)) {

            // then
            var iterator = records.iterator();
            var first = iterator.next();
            assertThat(first.get("id").asString()).isEqualTo("r1");
            assertThat(first.get("count").intValue()).isEqualTo(3);
            assertThat(first.get("active").booleanValue()).isTrue();
            assertThat(first.get("value").isNull()).isTrue();
            assertThat(first.path("absent").isMissingNode()).isTrue();
            assertThatThrownBy(iterator::hasNext).isInstanceOf(JacksonException.class);
        }
    }
}
