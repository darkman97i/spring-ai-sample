package com.openkm.test.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class DateToolsService {

    @Tool(name = "getSystemDate", description = "Get the current system date and time")
    public String getSystemDate() {
        log.info("=== MCP TOOL getSystemDate() CALLED ===");

        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String result = String.format("Current system date and time: %s", formattedDate);
        log.info("Returning system date: {}", result);

        return result;
    }

    @Tool(name = "getTimestamp", description = "Get the current timestamp in milliseconds")
    public String getTimestamp() {
        log.info("=== MCP TOOL getTimestamp() CALLED ===");

        long timestamp = System.currentTimeMillis();

        String result = String.format("Current timestamp: %d", timestamp);
        log.info("Returning timestamp: {}", result);

        return result;
    }
}
