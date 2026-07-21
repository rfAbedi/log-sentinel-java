package com.logsentinel.api.alert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AlertRepositoryTest {

    @Autowired
    private AlertRepository repository;

    @Test
    void savesAndFindsAlert() {
        AlertEntity alert = new AlertEntity(
                "gateway-errors",
                "gateway",
                Instant.parse("2025-07-01T12:56:07Z"),
                "Payment gateway timeout",
                "ERROR");

        AlertEntity saved = repository.saveAndFlush(alert);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getRuleId()).isEqualTo("gateway-errors");
                    assertThat(found.getComponent()).isEqualTo("gateway");
                    assertThat(found.getTriggeredAt())
                            .isEqualTo(Instant.parse("2025-07-01T12:56:07Z"));
                    assertThat(found.getMessage()).isEqualTo("Payment gateway timeout");
                    assertThat(found.getLevel()).isEqualTo("ERROR");
                });
    }

    @Test
    void findsAlertsNewestFirstWithDeterministicTieBreaking() {
        Instant olderTime = Instant.parse("2025-07-01T12:00:00Z");
        Instant newerTime = Instant.parse("2025-07-01T13:00:00Z");
        AlertEntity older = repository.save(alert("older", olderTime));
        AlertEntity newerFirst = repository.save(alert("newer-first", newerTime));
        AlertEntity newerSecond = repository.save(alert("newer-second", newerTime));
        repository.flush();

        Page<AlertEntity> alerts = repository.findAll(PageRequest.of(
                0,
                2,
                Sort.by(Sort.Order.desc("triggeredAt"), Sort.Order.desc("id"))));

        assertThat(alerts.getContent())
                .extracting(AlertEntity::getId)
                .containsExactly(newerSecond.getId(), newerFirst.getId());
        assertThat(alerts.getTotalElements()).isEqualTo(3);
        assertThat(alerts.getTotalPages()).isEqualTo(2);
    }

    private AlertEntity alert(String ruleId, Instant triggeredAt) {
        return new AlertEntity(
                ruleId,
                "gateway",
                triggeredAt,
                "Test alert",
                "ERROR");
    }
}
