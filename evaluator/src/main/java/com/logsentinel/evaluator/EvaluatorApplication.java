package com.logsentinel.evaluator;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.kafka.LogEventDeserializer;
import com.logsentinel.evaluator.alert.PostgresAlertRepository;
import com.logsentinel.evaluator.kafka.KafkaLogEventConsumer;
import com.logsentinel.evaluator.processing.EvaluatorBatchProcessor;
import com.logsentinel.evaluator.rule.RuleEvaluationService;
import com.logsentinel.evaluator.rule.config.RuleConfiguration;
import com.logsentinel.evaluator.rule.config.RuleConfigurationValidator;
import com.logsentinel.evaluator.rule.config.RuleValidationError;
import com.logsentinel.evaluator.rule.config.YamlRuleConfigurationLoader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.postgresql.ds.PGSimpleDataSource;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EvaluatorApplication {

    private EvaluatorApplication() {
    }

    public static void main(String[] args) throws Exception {
        EvaluatorSettings settings = EvaluatorSettings.from(System.getenv());
        List<RuleConfiguration> rules = loadRules(settings);
        PGSimpleDataSource dataSource = createDataSource(settings);
        AtomicBoolean running = new AtomicBoolean(true);

        try (KafkaConsumer<String, LogEvent> kafkaConsumer = new KafkaConsumer<>(
                kafkaProperties(settings))) {
            Thread shutdownHook = new Thread(() -> {
                running.set(false);
                kafkaConsumer.wakeup();
            }, "evaluator-shutdown");
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            try {
                KafkaLogEventConsumer consumer = new KafkaLogEventConsumer(
                        kafkaConsumer,
                        settings.kafkaTopic());
                RuleEvaluationService ruleEvaluationService = new RuleEvaluationService(rules);
                PostgresAlertRepository alertRepository = new PostgresAlertRepository(dataSource);
                EvaluatorBatchProcessor processor = new EvaluatorBatchProcessor(
                        consumer,
                        ruleEvaluationService,
                        alertRepository);

                new EvaluatorLoop(processor::pollOnce, settings.pollTimeout())
                        .run(running::get);
            } catch (WakeupException exception) {
                if (running.get()) {
                    throw exception;
                }
            } finally {
                removeShutdownHook(shutdownHook);
            }
        }
    }

    private static List<RuleConfiguration> loadRules(EvaluatorSettings settings) throws Exception {
        List<RuleConfiguration> rules = new YamlRuleConfigurationLoader().load(settings.rulesFile());
        List<RuleValidationError> errors = new RuleConfigurationValidator().validate(rules);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid rule configuration: " + errors);
        }
        return rules;
    }

    private static PGSimpleDataSource createDataSource(EvaluatorSettings settings) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(settings.databaseUrl());
        dataSource.setUser(settings.databaseUser());
        dataSource.setPassword(settings.databasePassword());
        return dataSource;
    }

    private static Properties kafkaProperties(EvaluatorSettings settings) {
        Properties properties = new Properties();
        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                settings.kafkaBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, settings.kafkaGroupId());
        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                LogEventDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    private static void removeShutdownHook(Thread shutdownHook) {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ignored) {
            // The JVM is already shutting down and is running this hook.
        }
    }
}
