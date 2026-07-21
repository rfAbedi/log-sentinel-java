package com.logsentinel.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesAlertEndpointInOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("LogSentinel Alerts API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths['/alerts'].get.summary").value("List alerts"))
                .andExpect(jsonPath("$.paths['/alerts'].get.parameters[?(@.name == 'page')].schema.minimum")
                        .value(0))
                .andExpect(jsonPath("$.paths['/alerts'].get.parameters[?(@.name == 'size')].schema.maximum")
                        .value(100))
                .andExpect(jsonPath("$.paths['/alerts'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/alerts'].get.responses['400']").exists());
    }

    @Test
    void exposesSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));
    }
}
