package com.logsentinel.api.alert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
}
