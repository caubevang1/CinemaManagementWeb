package com.cinemaweb.API.Cinema.Web.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupLogger {

    private final Environment environment;

    public StartupLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String baseUrl = "http://localhost:" + port + contextPath;

        log.info("Server đang chạy ở: {}", baseUrl);
        log.info("Swagger UI: {}/swagger-ui.html", baseUrl);
    }
}
