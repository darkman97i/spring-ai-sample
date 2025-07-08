package com.openkm.test.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class DateToolsService {

    @Tool(name="getSystemDate", description = "Get the current system date and time in my server")
    public String getSystemDate() {
        log.info("=== MCP TOOL getSystemDate() CALLED ===");

        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String result = String.format("Current system date and time: %s", formattedDate);
        log.info("Returning system date: {}", result);

        return result;
    }

    @Tool(name="getTimestamp", description = "Get the current timestamp in milliseconds in my server")
    public String getTimestamp() {
        log.info("=== MCP TOOL getTimestamp() CALLED ===");

        long timestamp = System.currentTimeMillis();

        String result = String.format("Current timestamp: %d", timestamp);
        log.info("Returning timestamp: {}", result);

        return result;
    }

    @Tool(description = "Get basic OpenKM ( document management system ) application information including version, build, and configuration details")
    public String getAppInfo() {
        log.debug("getAppInfo() called via MCP");

        try {
            StringBuilder info = new StringBuilder();
            info.append("OpenKM Application Information:\n");
            info.append("- Name: OpenKM\n");
            info.append("- Version: 8.1.12").append("\n");
            info.append("- Build: 125").append("\n");
            info.append("- Description: Open Document Management System\n");
            info.append("- Organization: OpenKM Knowledge Management System S.L.\n");
            info.append("- URL: https://www.openkm.com\n");
            info.append("- Maintenance: PRO").append("\n");

            return info.toString();

        } catch (Exception e) {
            log.error("Error getting app info", e);
            return "Error: " + e.getMessage();
        }
    }
}
