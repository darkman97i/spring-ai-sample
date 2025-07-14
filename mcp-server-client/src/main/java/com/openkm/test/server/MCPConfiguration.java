package com.openkm.test.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MCPConfiguration {

    @Autowired
    private DateToolsService dateToolsService;

    @Bean
    public ToolCallbackProvider testMCPTools() {
        log.info("=== REGISTERING Test MCP Tools ===");
        log.info("DateToolsService: {}", dateToolsService != null ? "INJECTED" : "NULL");

        ToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(dateToolsService)
                .build();

        log.info("ToolCallbackProvider created: {}", provider != null ? "SUCCESS" : "FAILED");
        log.info("Available tools: {}", provider.getToolCallbacks().length);

        return provider;
    }
}
