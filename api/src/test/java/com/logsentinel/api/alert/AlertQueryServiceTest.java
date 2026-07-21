package com.logsentinel.api.alert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertQueryServiceTest {

    @Mock
    private AlertRepository repository;

    @InjectMocks
    private AlertQueryService service;

    @Test
    void convertsOrderedEntitiesToResponses() {
        AlertEntity alert = new AlertEntity(
                "gateway-errors",
                "gateway",
                Instant.parse("2025-07-01T12:56:07Z"),
                "Payment gateway timeout",
                "ERROR");
        when(repository.findAllByOrderByTriggeredAtDescIdDesc()).thenReturn(List.of(alert));

        List<AlertResponse> responses = service.listAlerts();

        assertThat(responses).containsExactly(new AlertResponse(
                null,
                "gateway-errors",
                "gateway",
                Instant.parse("2025-07-01T12:56:07Z"),
                "Payment gateway timeout",
                "ERROR"));
    }

    @Test
    void returnsEmptyListWhenThereAreNoAlerts() {
        when(repository.findAllByOrderByTriggeredAtDescIdDesc()).thenReturn(List.of());

        assertThat(service.listAlerts()).isEmpty();
    }
}
