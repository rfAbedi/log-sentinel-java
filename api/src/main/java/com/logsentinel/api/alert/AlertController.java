package com.logsentinel.api.alert;

import com.logsentinel.api.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
@Tag(name = "Alerts", description = "Query generated alerts")
public class AlertController {

    private final AlertQueryService queryService;

    public AlertController(AlertQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(
            summary = "List alerts",
            description = "Returns alerts ordered by trigger time descending and ID descending"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "A page of alerts",
                    content = @Content(schema = @Schema(implementation = AlertPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public AlertPageResponse listAlerts(
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of alerts per page, from 1 to 100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return queryService.listAlerts(page, size);
    }
}
