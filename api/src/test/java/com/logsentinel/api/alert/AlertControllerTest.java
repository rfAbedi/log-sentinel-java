package com.logsentinel.api.alert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertQueryService queryService;

    @Test
    void returnsAlertsAsJsonInServiceOrder() throws Exception {
        when(queryService.listAlerts()).thenReturn(List.of(
                new AlertResponse(
                        2L,
                        "newer-rule",
                        "gateway",
                        Instant.parse("2025-07-01T13:00:00Z"),
                        "Newer alert",
                        "ERROR"),
                new AlertResponse(
                        1L,
                        "older-rule",
                        "payment",
                        Instant.parse("2025-07-01T12:00:00Z"),
                        "Older alert",
                        "WARN")));

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].ruleId").value("newer-rule"))
                .andExpect(jsonPath("$[0].component").value("gateway"))
                .andExpect(jsonPath("$[0].triggeredAt").value("2025-07-01T13:00:00Z"))
                .andExpect(jsonPath("$[0].message").value("Newer alert"))
                .andExpect(jsonPath("$[0].level").value("ERROR"))
                .andExpect(jsonPath("$[1].id").value(1));
    }

    @Test
    void returnsEmptyJsonArrayWhenThereAreNoAlerts() throws Exception {
        when(queryService.listAlerts()).thenReturn(List.of());

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
