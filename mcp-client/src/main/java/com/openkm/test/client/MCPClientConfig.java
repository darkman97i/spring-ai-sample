package com.openkm.test.client;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPClientConfig {

	@Bean
	public SyncMcpToolCallbackProvider syncMcpToolCallbackProvider() {
		return new SyncMcpToolCallbackProvider();
	}
}
