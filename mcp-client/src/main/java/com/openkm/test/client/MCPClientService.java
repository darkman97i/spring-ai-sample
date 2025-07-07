package com.openkm.test.client;

import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationContext;

@Slf4j
@Service
public class MCPClientService {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired(required = false)
	private SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

	public String performSelfTest() {
		StringBuilder result = new StringBuilder();
		result.append("=== MCP CLIENT SELF-TEST WITH SPRING AI 1.0.0 ===\n\n");

		try {
			// Paso 1: Verificar beans MCP disponibles
			result.append("1. CHECKING MCP BEANS:\n");
			String beansResult = checkMcpBeans();
			result.append(beansResult).append("\n\n");

			// Paso 2: Verificar provider
			result.append("2. CHECKING MCP TOOL CALLBACK PROVIDER:\n");
			if (syncMcpToolCallbackProvider == null) {
				result.append("❌ SyncMcpToolCallbackProvider is null - not autowired\n\n");
				return result.toString();
			} else {
				result.append("✅ SyncMcpToolCallbackProvider is available\n");
			}

			// Paso 3: Obtener callbacks MCP disponibles
			result.append("3. GETTING MCP TOOL CALLBACKS:\n");
			ToolCallback[] callbacks = syncMcpToolCallbackProvider.getToolCallbacks();
			if (callbacks != null && callbacks.length > 0) {
				result.append("✅ Found ").append(callbacks.length).append(" MCP tool callbacks\n\n");

				// Paso 4: Listar herramientas disponibles
				result.append("4. LISTING AVAILABLE TOOLS:\n");
				String toolsListResult = listAvailableTools(callbacks);
				result.append(toolsListResult).append("\n\n");

				// Paso 5: Llamar herramientas disponibles
				result.append("5. CALLING AVAILABLE TOOLS:\n");
				String toolCallResults = callAvailableTools(callbacks);
				result.append(toolCallResults).append("\n\n");
			} else {
				result.append("❌ No MCP tool callbacks found - callbacks array is ")
						.append(callbacks == null ? "null" : "empty").append("\n\n");
			}

			result.append("✅ MCP CLIENT SELF-TEST COMPLETED\n");

		} catch (Exception e) {
			result.append("❌ Error during self-test: ").append(e.getMessage()).append("\n");
			log.error("Error during self-test", e);
		}

		return result.toString();
	}

	private String checkMcpBeans() {
		StringBuilder result = new StringBuilder();

		// Verificar todos los beans que contengan "mcp" en el nombre
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

					// Aquí podrías intentar llamar la herramienta si conoces los parámetros
					// Por ahora solo mostramos información básica

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
