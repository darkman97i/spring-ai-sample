package com.openkm.test.controller;

import com.openkm.test.config.MCPClientService;
import com.openkm.test.config.ManualMcpClientConfiguration;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class TestController {

	@Autowired
	OpenAiChatModel openAiChatModel;

	@Autowired
	private MCPClientService mcpClientService;

	@Autowired(required = false)
	private ManualMcpClientConfiguration.ManualMcpClientManager manualManager;

	@GetMapping(value = "/test", produces = "text/plain")
	public String performSelfTest() {
		log.info("Starting MCP client self-test with manual configuration...");
		return mcpClientService.performSelfTest();
	}

	@GetMapping(value = "/init", produces = "text/plain")
	public String initializeManualClients() {
		log.info("Initializing MCP clients manually...");
		return mcpClientService.initializeManualClients();
	}

	@GetMapping(value = "/shutdown", produces = "text/plain")
	public String shutdownManualClients() {
		log.info("Shutting down MCP clients manually...");
		return mcpClientService.shutdownManualClients();
	}

	@GetMapping(value = "/status", produces = "text/plain")
	public String checkClientStatus() {
		StringBuilder result = new StringBuilder();
		result.append("=== MCP CLIENT STATUS ===\n\n");

		if (manualManager == null) {
			result.append("❌ ManualMcpClientManager not available\n");
			return result.toString();
		}

		result.append("Manager initialized: ").append(manualManager.isInitialized() ? "✅ Yes" : "❌ No").append("\n");

		if (manualManager.isInitialized()) {
			List<McpSyncClient> clients = manualManager.getMcpSyncClients();
			result.append("Number of clients: ").append(clients.size()).append("\n");
			result.append("Clients ready: ").append(manualManager.areClientsReady() ? "✅ Yes" : "❌ No").append("\n\n");

			for (int i = 0; i < clients.size(); i++) {
				McpSyncClient client = clients.get(i);
				result.append("Client ").append(i + 1).append(":\n");
				try {
					var capabilities = client.getServerCapabilities();
					result.append("  - Status: ✅ Ready\n");
					result.append("  - Capabilities: ").append(capabilities != null ? capabilities.toString() : "null").append("\n");
				} catch (Exception e) {
					result.append("  - Status: ❌ Not ready\n");
					result.append("  - Error: ").append(e.getMessage()).append("\n");
				}
				result.append("\n");
			}
		}

		return result.toString();
	}

	@GetMapping(value = "/chat", produces = "text/plain")
	public String performChatTest() {
		StringBuilder result = new StringBuilder();
		result.append("=== MCP CHAT TEST ===\n\n");

		try {
			if (manualManager == null || !manualManager.isInitialized()) {
				result.append("❌ Manual MCP clients not initialized. Please call /init first.\n");
				return result.toString();
			}

			// Verificar que los clientes estén listos antes de continuar
			if (!manualManager.areClientsReady()) {
				result.append("❌ MCP clients are not ready yet. They may still be completing the handshake.\n");
				result.append("Please wait a moment and try again.\n");
				return result.toString();
			}

			List<McpSyncClient> clientsToUse = manualManager.getMcpSyncClients();
			result.append("Using manual MCP clients: ").append(clientsToUse.size()).append("\n");
			result.append("✅ All clients are ready and connected\n\n");

			var mcpToolProvider = new SyncMcpToolCallbackProvider(clientsToUse);
			ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultToolCallbacks(mcpToolProvider).build();

			List<String> userQuestions = List.of(
					"What is the current system date and time in the openkm server?",
					"What is the openkm version?"
			);

			for (String userQuestion : userQuestions) {
				result.append("> USER: ").append(userQuestion).append("\n");
				try {
					String response = chatClient.prompt(userQuestion).call().content();
					result.append("> ASSISTANT: ").append(response).append("\n");
				} catch (Exception e) {
					result.append("> ERROR: ").append(e.getMessage()).append("\n");
					log.error("Error during chat question: " + userQuestion, e);
				}
				result.append("---\n");
			}

		} catch (Exception e) {
			result.append("❌ Error during chat test: ").append(e.getMessage()).append("\n");
			log.error("Error during chat test", e);
		}

		return result.toString();
	}
}
