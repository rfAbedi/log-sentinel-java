package com.logsentinel.api.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @Column(nullable = false)
    private String component;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    private String level;

    protected AlertEntity() {
    }

    public AlertEntity(
            String ruleId,
            String component,
            Instant triggeredAt,
            String message,
            String level
    ) {
        this.ruleId = ruleId;
        this.component = component;
        this.triggeredAt = triggeredAt;
        this.message = message;
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getComponent() {
        return component;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public String getMessage() {
        return message;
    }

    public String getLevel() {
        return level;
    }
}
