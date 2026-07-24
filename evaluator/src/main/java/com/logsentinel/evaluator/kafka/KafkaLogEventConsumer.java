package com.logsentinel.evaluator.kafka;

import com.logsentinel.contracts.LogEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class KafkaLogEventConsumer implements LogEventBatchConsumer {

    private final Consumer<String, LogEvent> consumer;
    private final String topic;

    public KafkaLogEventConsumer(Consumer<String, LogEvent> consumer, String topic) {
        this.consumer = consumer;
        this.topic = topic;
    }

    @Override
    public List<LogEvent> pollOnce(Duration timeout) {
        consumer.subscribe(List.of(topic));
        ConsumerRecords<String, LogEvent> records = consumer.poll(timeout);
        List<LogEvent> events = new ArrayList<>();
        records.forEach(record -> events.add(record.value()));
        return List.copyOf(events);
    }
}
