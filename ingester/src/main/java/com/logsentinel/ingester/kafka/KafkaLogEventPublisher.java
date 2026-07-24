package com.logsentinel.ingester.kafka;

import com.logsentinel.contracts.LogEvent;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.Future;

public final class KafkaLogEventPublisher implements LogEventPublisher {

    private final Producer<String, LogEvent> producer;
    private final String topic;

    public KafkaLogEventPublisher(Producer<String, LogEvent> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public Future<RecordMetadata> publish(LogEvent event) {
        ProducerRecord<String, LogEvent> record = new ProducerRecord<>(
                topic,
                event.component(),
                event);
        return producer.send(record);
    }
}
