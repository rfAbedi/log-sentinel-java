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
        when(queryService.listAlerts(0, 20)).thenReturn(new AlertPageResponse(
                List.of(new AlertResponse(
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
                        "WARN")),
                0,
                20,
                2,
                1));

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].ruleId").value("newer-rule"))
                .andExpect(jsonPath("$.content[0].component").value("gateway"))
                .andExpect(jsonPath("$.content[0].triggeredAt").value("2025-07-01T13:00:00Z"))
                .andExpect(jsonPath("$.content[0].message").value("Newer alert"))
                .andExpect(jsonPath("$.content[0].level").value("ERROR"))
                .andExpect(jsonPath("$.content[1].id").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void passesRequestedPageAndSizeToService() throws Exception {
        when(queryService.listAlerts(2, 5)).thenReturn(new AlertPageResponse(
                List.of(),
                2,
                5,
                0,
                0));

        mockMvc.perform(get("/alerts").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
}
