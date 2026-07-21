package com.logsentinel.api.alert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertQueryService {

    private static final Sort NEWEST_FIRST = Sort.by(
            Sort.Order.desc("triggeredAt"),
            Sort.Order.desc("id"));

    private final AlertRepository repository;

    public AlertQueryService(AlertRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AlertPageResponse listAlerts(int page, int size) {
        Page<AlertResponse> alerts = repository.findAll(PageRequest.of(page, size, NEWEST_FIRST))
                .map(AlertResponse::from);

        return AlertPageResponse.from(alerts);
    }
}
