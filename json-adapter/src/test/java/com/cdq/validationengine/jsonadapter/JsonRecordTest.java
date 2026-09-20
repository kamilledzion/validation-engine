package com.cdq.validationengine.jsonadapter;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class JsonRecordTest {

    @Test
    void shouldReadFields() {
        // given
        var record = new JsonRecord(JsonMapper.builder().build().readTree("""
                {
                  "id": "r1",
                  "name": "Lodz",
                  "count": 3,
                  "active": true,
                  "address": {
                    "city": "Lodz"
                  },
                  "value": null
                }
                """));

        // when - then
        assertThat(record.id()).isEqualTo("r1");
        assertThat(record.field("name")).isEqualTo("Lodz");
        assertThat(record.field("count")).isEqualTo(3);
        assertThat(record.field("active")).isEqualTo(true);
        assertThat(record.field("value")).isNull();
        assertThat(record.field("missing")).isNull();
        assertThatThrownBy(() -> record.field("address"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported field type");
    }
}
