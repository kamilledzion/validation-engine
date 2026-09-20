package com.cdq.validationengine.jsonadapter;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class JsonRecordReader {

    private final ObjectReader reader = JsonMapper.builder().build().readerFor(JsonNode.class);

    public Stream<JsonNode> read(InputStream input) {
        MappingIterator<JsonNode> records = reader.readValues(input);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(records, Spliterator.ORDERED), false)
                .onClose(records::close);
    }
}
