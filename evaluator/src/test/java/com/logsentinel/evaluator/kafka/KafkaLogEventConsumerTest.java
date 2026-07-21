package com.logsentinel.evaluator.kafka;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaLogEventConsumerTest {

    @Test
    void pollsAndReturnsEventsFromKafka() {
        LogEvent event = new LogEvent(
                "event-1",
                "gateway",
                Instant.parse("2025-07-01T12:56:07.451Z"),
                LogLevel.ERROR,
                "http-worker-1",
                "com.example.GatewayClient",
                "Payment gateway timeout",
                "app.log",
                42);

        ConsumerRecords<String, LogEvent> records = new ConsumerRecords<>(Map.of(
                new TopicPartition("log-events", 0),
                List.of(new ConsumerRecord<>("log-events", 0, 0L, "key", event))));

        Consumer<String, LogEvent> consumer = createConsumer(records);
        KafkaLogEventConsumer kafkaLogEventConsumer = new KafkaLogEventConsumer(consumer, "log-events");

        List<LogEvent> consumed = kafkaLogEventConsumer.pollOnce(Duration.ofSeconds(1));

        assertEquals(List.of(event), consumed);
    }

    private Consumer<String, LogEvent> createConsumer(ConsumerRecords<String, LogEvent> records) {
        return (Consumer<String, LogEvent>) Proxy.newProxyInstance(
                Consumer.class.getClassLoader(),
                new Class<?>[]{Consumer.class},
                (proxy, method, args) -> {
                    if ("poll".equals(method.getName())) {
                        return records;
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    if ("subscribe".equals(method.getName())) {
                        return null;
                    }
                    return null;
                });
    }
}
