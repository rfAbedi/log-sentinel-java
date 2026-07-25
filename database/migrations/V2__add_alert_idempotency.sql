ALTER TABLE alerts
    ADD COLUMN source_event_id VARCHAR(255);

ALTER TABLE alerts
    ADD CONSTRAINT uq_alerts_rule_source_event
        UNIQUE (rule_id, source_event_id);
