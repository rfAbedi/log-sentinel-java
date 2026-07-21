package com.logsentinel.api.alert;

import org.springframework.data.domain.Page;

import java.util.List;

public record AlertPageResponse(
        List<AlertResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static AlertPageResponse from(Page<AlertResponse> alerts) {
        return new AlertPageResponse(
                alerts.getContent(),
                alerts.getNumber(),
                alerts.getSize(),
                alerts.getTotalElements(),
                alerts.getTotalPages());
    }
}
