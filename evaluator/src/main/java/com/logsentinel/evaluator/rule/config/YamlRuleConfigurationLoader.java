package com.logsentinel.evaluator.rule.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.logsentinel.contracts.LogLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public final class YamlRuleConfigurationLoader {

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public List<RuleConfiguration> load(Path yamlFile) throws IOException {
        RulesDocument document = objectMapper.readValue(yamlFile.toFile(), RulesDocument.class);
        if (document.rules() == null) {
            return List.of();
        }

        return document.rules().stream()
                .map(this::toRuleConfiguration)
                .toList();
    }

    private RuleConfiguration toRuleConfiguration(RuleDefinition definition) {
        return switch (definition.type()) {
            case LOG_LEVEL -> new LogLevelRuleConfiguration(
                    definition.id(),
                    definition.component(),
                    definition.level());
            case LEVEL_COUNT -> new LevelCountRuleConfiguration(
                    definition.id(),
                    definition.component(),
                    definition.level(),
                    definition.threshold(),
                    parseWindow(definition.window()));
            case TOTAL_LOG_RATE -> new TotalLogRateRuleConfiguration(
                    definition.id(),
                    definition.component(),
                    definition.threshold(),
                    parseWindow(definition.window()));
        };
    }

    private Duration parseWindow(String window) {
        return window == null ? null : Duration.parse(window);
    }

    public record RulesDocument(List<RuleDefinition> rules) {
    }

    public record RuleDefinition(
            RuleType type,
            String id,
            String component,
            LogLevel level,
            int threshold,
            String window
    ) {
    }

    public enum RuleType {
        LOG_LEVEL,
        LEVEL_COUNT,
        TOTAL_LOG_RATE
    }
}
