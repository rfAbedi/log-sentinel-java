package com.logsentinel.ingester;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.kafka.LogEventSerializer;
import com.logsentinel.ingester.file.ExistingLogFileReader;
import com.logsentinel.ingester.kafka.KafkaLogEventPublisher;
import com.logsentinel.ingester.mapping.LogEventMapper;
import com.logsentinel.ingester.parser.LogLineParser;
import com.logsentinel.ingester.processing.LogFileProcessor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public final class IngesterApplication {

    private IngesterApplication() {
    }

    public static void main(String[] args) throws Exception {
        IngesterSettings settings = IngesterSettings.from(System.getenv());

        try (Producer<String, LogEvent> producer = new KafkaProducer<>(producerProperties(settings))) {
            LogFileProcessor processor = new LogFileProcessor(
                    new LogLineParser(),
                    new LogEventMapper(settings.sourceTimeZone()),
                    new KafkaLogEventPublisher(producer, settings.kafkaTopic()));

            OneShotIngester ingester = new OneShotIngester(
                    new ExistingLogFileReader(settings.logDirectory()),
                    processor);

            int processedFiles = ingester.run();
            producer.flush();
            System.out.printf("Processed %d log file(s).%n", processedFiles);
        }
    }

    private static Properties producerProperties(IngesterSettings settings) {
        Properties properties = new Properties();
        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                settings.kafkaBootstrapServers());
        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                LogEventSerializer.class.getName());
        return properties;
    }
}
