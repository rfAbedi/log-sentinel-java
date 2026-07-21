package com.logsentinel.api.alert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                List.of(alert),
                PageRequest.of(1, 1),
                3));

        AlertPageResponse response = service.listAlerts(1, 1);

        assertThat(response.content()).containsExactly(new AlertResponse(
                null,
                "gateway-errors",
                "gateway",
                Instant.parse("2025-07-01T12:56:07Z"),
                "Payment gateway timeout",
                "ERROR"));
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    void requestsThePageWithNewestFirstOrdering() {
        when(repository.findAll(argThat((Pageable pageable) ->
                pageable.getPageNumber() == 2
                        && pageable.getPageSize() == 5
                        && pageable.getSort().getOrderFor("triggeredAt").isDescending()
                        && pageable.getSort().getOrderFor("id").isDescending())))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

        AlertPageResponse response = service.listAlerts(2, 5);

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(5);
    }
}
