package com.logsentinel.api.alert;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertQueryService {

    private final AlertRepository repository;

    public AlertQueryService(AlertRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> listAlerts() {
        return repository.findAllByOrderByTriggeredAtDescIdDesc().stream()
                .map(AlertResponse::from)
                .toList();
    }
}
