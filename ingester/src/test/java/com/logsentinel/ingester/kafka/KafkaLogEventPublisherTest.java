package com.logsentinel.ingester.kafka;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaLogEventPublisherTest {

    @Test
    void publishesEventToConfiguredTopicUsingComponentAsKey() throws Exception {
        Serializer<LogEvent> valueSerializer = (topic, event) -> new byte[0];
        MockProducer<String, LogEvent> producer = new MockProducer<>(
                true,
                null,
                new StringSerializer(),
                valueSerializer);
        KafkaLogEventPublisher publisher = new KafkaLogEventPublisher(producer, "log-events");
        LogEvent event = event();

        Future<RecordMetadata> result = publisher.publish(event);

        List<ProducerRecord<String, LogEvent>> history = producer.history();
        assertEquals(1, history.size());
        assertEquals("log-events", history.getFirst().topic());
        assertEquals("payment-service", history.getFirst().key());
        assertEquals(event, history.getFirst().value());
        assertTrue(result.isDone());
        assertEquals("log-events", result.get().topic());
    }

    private LogEvent event() {
        return new LogEvent(
                "event-1",
                "payment-service",
                Instant.parse("2025-07-01T12:56:07.451Z"),
                LogLevel.ERROR,
                "http-worker-1",
                "com.example.GatewayClient",
                "Payment gateway timeout",
                "payment-service_2025-07-01_12-55-55.log",
                3);
    }
}
