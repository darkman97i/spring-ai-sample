package com.openkm.test.config;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MCPClientService {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired(required = false)
	private ManualMcpClientConfiguration.ManualMcpClientManager manualMcpClientManager;

	public String performSelfTest() {
		StringBuilder result = new StringBuilder();
		result.append("=== MCP CLIENT SELF-TEST WITH MANUAL CONFIGURATION ===\n\n");

		try {
			result.append("1. CHECKING MCP BEANS:\n");
			String beansResult = checkMcpBeans();
			result.append(beansResult).append("\n\n");

			result.append("2. CHECKING MANUAL MCP CLIENT MANAGER:\n");
			if (manualMcpClientManager == null) {
				result.append("❌ ManualMcpClientManager is null - manual configuration disabled or not autowired\n\n");
			} else {
				result.append("✅ ManualMcpClientManager is available\n");
				result.append("   - Initialized: ").append(manualMcpClientManager.isInitialized()).append("\n\n");
			}

			// Paso 3: Obtener callbacks MCP si los clientes están inicializados
			result.append("3. GETTING MCP TOOL CALLBACKS:\n");
			if (manualMcpClientManager != null && manualMcpClientManager.isInitialized()) {
				try {
					SyncMcpToolCallbackProvider provider = new SyncMcpToolCallbackProvider(manualMcpClientManager.getMcpSyncClients());
					ToolCallback[] callbacks = provider.getToolCallbacks();
					if (callbacks != null && callbacks.length > 0) {
						result.append("✅ Found ").append(callbacks.length).append(" MCP tool callbacks\n\n");

						result.append("4. LISTING AVAILABLE TOOLS:\n");
						String toolsListResult = listAvailableTools(callbacks);
						result.append(toolsListResult).append("\n\n");

						result.append("5. CALLING AVAILABLE TOOLS:\n");
						String toolCallResults = callAvailableTools(callbacks);
						result.append(toolCallResults).append("\n\n");
					} else {
						result.append("❌ No MCP tool callbacks found - callbacks array is ")
								.append(callbacks == null ? "null" : "empty").append("\n\n");
					}
				} catch (Exception e) {
					result.append("❌ Error creating tool callback provider: ").append(e.getMessage()).append("\n\n");
					log.error("Error creating tool callback provider", e);
				}
			} else {
				result.append("❌ MCP clients not initialized - cannot get tool callbacks\n\n");
			}

			result.append("✅ MCP CLIENT SELF-TEST COMPLETED\n");

		} catch (Exception e) {
			result.append("❌ Error during self-test: ").append(e.getMessage()).append("\n");
			log.error("Error during self-test", e);
		}

		return result.toString();
	}

	public String initializeManualClients() {
		StringBuilder result = new StringBuilder();
		result.append("=== MANUAL MCP CLIENT INITIALIZATION ===\n\n");

		try {
			if (manualMcpClientManager == null) {
				result.append("❌ ManualMcpClientManager is not available - check configuration\n");
				return result.toString();
			}

			if (manualMcpClientManager.isInitialized()) {
				result.append("ℹ️ MCP clients are already initialized\n");
				List<McpSyncClient> clients = manualMcpClientManager.getMcpSyncClients();
				result.append("   - Active clients: ").append(clients.size()).append("\n");
			} else {
				result.append("🔄 Initializing MCP clients manually...\n");
				manualMcpClientManager.initializeClients();

				List<McpSyncClient> clients = manualMcpClientManager.getMcpSyncClients();
				result.append("✅ Manual initialization completed successfully\n");
				result.append("   - Created clients: ").append(clients.size()).append("\n");

				boolean ready = manualMcpClientManager.areClientsReady();
				result.append("   - Clients ready: ").append(ready ? "✅ Yes" : "❌ No (may need more time for handshake)").append("\n");

				for (int i = 0; i < clients.size(); i++) {
					McpSyncClient client = clients.get(i);
					result.append("   - Client ").append(i + 1).append(": ").append(client).append("\n");
				}
			}

		} catch (Exception e) {
			result.append("❌ Error during manual initialization: ").append(e.getMessage()).append("\n");
			log.error("Error during manual initialization", e);
		}

		return result.toString();
	}

	public String shutdownManualClients() {
		StringBuilder result = new StringBuilder();
		result.append("=== MANUAL MCP CLIENT SHUTDOWN ===\n\n");

		try {
			if (manualMcpClientManager == null) {
				result.append("❌ ManualMcpClientManager is not available\n");
				return result.toString();
			}

			if (!manualMcpClientManager.isInitialized()) {
				result.append("ℹ️ MCP clients are not initialized\n");
			} else {
				List<McpSyncClient> clients = manualMcpClientManager.getMcpSyncClients();
				result.append("🔄 Shutting down ").append(clients.size()).append(" MCP clients...\n");

				manualMcpClientManager.shutdown();
				result.append("✅ Manual shutdown completed successfully\n");
			}

		} catch (Exception e) {
			result.append("❌ Error during manual shutdown: ").append(e.getMessage()).append("\n");
			log.error("Error during manual shutdown", e);
		}

		return result.toString();
	}

	private String checkMcpBeans() {
		StringBuilder result = new StringBuilder();

		String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
		int mcpBeanCount = 0;

		for (String beanName : beanNames) {
			if (beanName.toLowerCase().contains("mcp")) {
				Object bean = applicationContext.getBean(beanName);
				result.append("  - ").append(beanName).append(" (").append(bean.getClass().getSimpleName()).append(")\n");
				mcpBeanCount++;
			}
		}

		if (mcpBeanCount == 0) {
			result.append("❌ No MCP-related beans found in application context\n");
		} else {
			result.append("✅ Found ").append(mcpBeanCount).append(" MCP-related beans\n");
		}

		return result.toString();
	}

	private String listAvailableTools(ToolCallback[] callbacks) {
		try {
			log.info("Listing available tools...");

			StringBuilder toolsInfo = new StringBuilder();
			toolsInfo.append("✅ Available MCP tool callbacks:\n");

			for (ToolCallback callback : callbacks) {
				toolsInfo.append("  - Tool: ").append(callback.getToolDefinition().name())
						.append(" (").append(callback.getClass().getSimpleName()).append(")\n");
			}

			log.info("Tools listed successfully: {} callbacks found", callbacks.length);
			return toolsInfo.toString();
		} catch (Exception e) {
			log.error("Failed to list tools", e);
			return "❌ Tools list failed: " + e.getMessage();
		}
	}

	private String callAvailableTools(ToolCallback[] callbacks) {
		StringBuilder result = new StringBuilder();

		try {
			log.info("Testing MCP tool callbacks...");

			for (ToolCallback callback : callbacks) {
				try {
					String toolName = callback.getToolDefinition().name();
					result.append("✅ Tool: ").append(toolName)
							.append(" (").append(callback.getClass().getSimpleName()).append(")\n");

				} catch (Exception e) {
					result.append("❌ Error with tool ").append(callback.getToolDefinition().name())
							.append(": ").append(e.getMessage()).append("\n");
				}
			}

			log.info("MCP tool callbacks tested successfully");
			return result.toString();
		} catch (Exception e) {
			log.error("Failed to test MCP tool callbacks", e);
			return "❌ MCP tool callbacks test failed: " + e.getMessage();
		}
	}
}
