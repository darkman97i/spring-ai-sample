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
//					"Sumarize in a paragraph the document with uuid: 984234f3-d9be-4df8-a21c-11401c41ade1",
//					"How many word have the document with uuid: 984234f3-d9be-4df8-a21c-11401c41ade1",
//					"How many metadata group defintion have the openkm application",
//					"Does the document with uuid: 984234f3-d9be-4df8-a21c-11401c41ade1 have metadata groups",
//					"In the case of the document with uuid: 984234f3-d9be-4df8-a21c-11401c41ade1 have metadata groups, show me them with their values",
//					"Search for documents with the name 'otas' in openkm and show the results",
//					"Search for documents with the name 'otas' in openkm and show the results. Then for each document show the properties",
//					"List the documents in the folder /okm:root/import"
//					"Get the uuids of the document in the folder /okm:root/import",
//					"Perform a query to find documents with the metadata field okp:doc_type.type = [\"project\"]",
//					"Utilizando únicamente el buscador, busca documentos cuyos metadatos identifiquen que son del tipo \"project\". " +
//							"Ten en cuenta la definición de los grupos de metadatos de la aplicación para identificar el campo de metadatos para utilizar en la búsqueda. " +
//							"Para cada documento devuelve los campos de metadatos de todos los grupos que tiene el documento asignados. " +
//							"Tienes que identificar los grupos de metadatos que cada documento tiene asignado y luego obtener el valor de cada grupo",
//					"Notas preliminares:" +
//						"* Los valores de lo campos de metadatos de tipo select se envían en el formato [\"valor\"]\n" +
//						"* Los valores de lo campos de metadatos de tipo fecha se envían en el formato \"yyyyMMddHHmms\"\n" +
//					"Lista de TODO:\n" +
//						"1- Obtén del grupo de metadatos \"Document type\" los tipos de documentos.\n" +
//						"2- Utilizando el contenido del documento con el uuid: 5531eca4-73f7-4e2b-bb97-138ce4313a17 intenta identificar el tipo de documento a partir de la lista de tipos de documentos que has obtenido en  el paso anterior. Si no eres capaz de identificar el tipo de documento para en este punto\n" +
//						"3- Añade el grupo de metadatos \"Document type\" al documento con el tipo de documento que has identificado (debes utilizar exactamente el valore de la lista de los tipos.\n" +
//						"4- En el caso que se el tipo de documento sea \"invoice\" realiza los siguientes pasos ( en caso contrario para ):\n" +
//						"	4.1- Obtén la definicion del grupo de metadatos \"Invoice\"\n" +
//						"   4.2- Intenta extraer del contenido del documento los datos para los campos del grupo de metadatos \"Invoice\"\n" +
//						"   4.3- Añade al documento el grupo de metadatos \"Invoice\" con los valores capturados en el pado anterior. Tienes que enviar los valores en el formato correspondiente ( ten cuidado con los campos de tipo Select y los Inputs de subtipo fecha )\n"
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
