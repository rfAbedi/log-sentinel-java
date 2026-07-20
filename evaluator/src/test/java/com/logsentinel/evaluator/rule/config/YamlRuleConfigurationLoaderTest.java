package com.logsentinel.evaluator.rule.config;

import com.logsentinel.contracts.LogLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRuleConfigurationLoaderTest {

    @TempDir
    Path directory;

    private final YamlRuleConfigurationLoader loader = new YamlRuleConfigurationLoader();

    @Test
    void loadsEveryRuleTypeInYamlOrder() throws IOException {
        Path yamlFile = writeYaml("""
                rules:
                  - type: LOG_LEVEL
                    id: gateway-error
                    component: gateway
                    level: ERROR
                  - type: LEVEL_COUNT
                    id: payment-warnings
                    component: payment
                    level: WARN
                    threshold: 5
                    window: PT1M
                  - type: TOTAL_LOG_RATE
                    id: api-rate
                    component: api
                    threshold: 100
                    window: PT30S
                """);

        List<RuleConfiguration> rules = loader.load(yamlFile);

        assertEquals(List.of(
                new LogLevelRuleConfiguration(
                        "gateway-error",
                        "gateway",
                        LogLevel.ERROR),
                new LevelCountRuleConfiguration(
                        "payment-warnings",
                        "payment",
                        LogLevel.WARN,
                        5,
                        Duration.ofMinutes(1)),
                new TotalLogRateRuleConfiguration(
                        "api-rate",
                        "api",
                        100,
                        Duration.ofSeconds(30))), rules);
    }

    @Test
    void loadsEmptyRuleList() throws IOException {
        Path yamlFile = writeYaml("rules: []\n");

        List<RuleConfiguration> rules = loader.load(yamlFile);

        assertTrue(rules.isEmpty());
    }

    private Path writeYaml(String yaml) throws IOException {
        return Files.writeString(directory.resolve("rules.yaml"), yaml);
    }
}
