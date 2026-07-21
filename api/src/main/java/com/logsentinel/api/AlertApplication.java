package com.logsentinel.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "LogSentinel Alerts API",
        version = "v1",
        description = "Read alerts generated from application log events"
))
public class AlertApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertApplication.class, args);
    }
}
