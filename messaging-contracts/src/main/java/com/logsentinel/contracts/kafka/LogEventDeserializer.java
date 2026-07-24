package com.logsentinel.contracts.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logsentinel.contracts.LogEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public final class LogEventDeserializer implements Deserializer<LogEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public LogEvent deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(data, LogEvent.class);
        } catch (IOException exception) {
            throw new SerializationException("Could not deserialize LogEvent", exception);
        }
    }
}
