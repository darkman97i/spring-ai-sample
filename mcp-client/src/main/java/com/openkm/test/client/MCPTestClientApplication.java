package com.openkm.test.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class MCPTestClientApplication {
	public static void main(String[] args) {
		SpringApplication.run(MCPTestClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner predefinedQuestions(OpenAiChatModel openAiChatModel,
												 List<McpSyncClient> mcpClients) {

		return args -> {

			// Inicializar manualmente los clientes MCP
			for (McpSyncClient client : mcpClients) {
				try {
					if (!client.isInitialized()) {
						client.initialize();
						System.out.println("Cliente MCP inicializado correctamente");
					}
				} catch (Exception e) {
					System.err.println("Error al inicializar el cliente MCP: " + e.getMessage());
					return; // Salir si no se puede inicializar
				}
			}

			var mcpToolProvider = new SyncMcpToolCallbackProvider(mcpClients);

			ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultToolCallbacks(mcpToolProvider).build();

			List<String> userQuestions = List.of(
					"What is the current system date and time in the openkm server?",
					"What is the openkm version?"
			);

			for (String userQuestion : userQuestions) {
				System.out.println("> USER: " + userQuestion);
				System.out.println("> ASSISTANT: " + chatClient.prompt(userQuestion).call().content());
				System.out.println("---"); // Separador entre preguntas
			}
		};
	}

	@Bean
	McpSyncClientCustomizer samplingCustomizer(Map<String, ChatClient> chatClients) {

		return (name, mcpClientSpec) -> {

			mcpClientSpec = mcpClientSpec.loggingConsumer(logingMessage -> {
				System.out.println("MCP LOGGING: [" + logingMessage.level() + "] " + logingMessage.data());
			});

			mcpClientSpec.sampling(llmRequest -> {
				var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
				String modelHint = llmRequest.modelPreferences().hints().get(0).name();

				ChatClient hintedChatClient = chatClients.entrySet().stream()
						.filter(e -> e.getKey().contains(modelHint)).findFirst()
						.orElseThrow().getValue();

				String response = hintedChatClient.prompt()
						.system(llmRequest.systemPrompt())
						.user(userPrompt)
						.call()
						.content();

				return CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build();
			});
			System.out.println("Customizing " + name);
		};
	}

	@Bean
	public Map<String, ChatClient> chatClients(List<ChatModel> chatModels) {

		return chatModels.stream().collect(Collectors.toMap(model -> model.getClass().getSimpleName().toLowerCase(),
				model -> ChatClient.builder(model).build()));

	}
}
