package com.logsentinel.api.alert;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertQueryService queryService;

    public AlertController(AlertQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public AlertPageResponse listAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return queryService.listAlerts(page, size);
    }
}
