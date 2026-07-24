package com.logsentinel.contracts.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logsentinel.contracts.LogEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public final class LogEventSerializer implements Serializer<LogEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public byte[] serialize(String topic, LogEvent event) {
        if (event == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(event);
        } catch (JsonProcessingException exception) {
            throw new SerializationException("Could not serialize LogEvent", exception);
        }
    }
}
