package com.openkm.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "mcp.manual.client", name = "enabled", havingValue = "true")
public class ManualMcpClientConfiguration {

    @Autowired
    private ManualMcpClientProperties properties;

    @Bean
    public ManualMcpClientManager manualMcpClientManager() {
        return new ManualMcpClientManager(properties);
    }

    public static class ManualMcpClientManager {

        private final ManualMcpClientProperties properties;
        private final List<McpSyncClient> mcpSyncClients = new ArrayList<>();
        private final ObjectMapper objectMapper = new ObjectMapper();
        private boolean initialized = false;

        public ManualMcpClientManager(ManualMcpClientProperties properties) {
            this.properties = properties;
            log.info("Manual MCP Client Manager created with {} connections configured",
                    properties.getConnections().size());
        }

        public synchronized void initializeClients() {
            if (initialized) {
                log.info("MCP clients already initialized, skipping...");
                return;
            }

            log.info("Starting manual initialization of MCP clients...");

            try {
                createSseTransportsAndClients();

                if (properties.getClient().isInitialized()) {
                    for (McpSyncClient client : mcpSyncClients) {
                        log.info("Initializing MCP client: {}", client);
                        try {
                            client.initialize();
                            // Esperar un poco para que se complete el handshake
                            Thread.sleep(1000);
                            // Verificar que la inicialización fue exitosa
                            var capabilities = client.getServerCapabilities();
                            log.info("MCP client initialized successfully. Server capabilities: {}", capabilities);
                        } catch (Exception e) {
                            log.warn("Failed to initialize MCP client fully: {}", e.getMessage());
                            // No lanzamos excepción para permitir que continúe
                        }
                    }
                }

                initialized = true;
                log.info("Manual MCP client initialization completed successfully. {} clients created",
                        mcpSyncClients.size());

            } catch (Exception e) {
                log.error("Error during manual MCP client initialization", e);
                throw new RuntimeException("Failed to initialize MCP clients manually", e);
            }
        }

        private void createSseTransportsAndClients() {
            for (Map.Entry<String, ManualMcpClientProperties.SseConnection> entry : properties.getConnections().entrySet()) {
                String connectionName = entry.getKey();
                ManualMcpClientProperties.SseConnection connection = entry.getValue();

                log.info("Creating SSE transport for connection: {} -> {}{}",
                        connectionName, connection.getUrl(), connection.getSseEndpoint());

                try {
                    var transport = HttpClientSseClientTransport.builder(connection.getUrl())
                            .sseEndpoint(connection.getSseEndpoint())
                            .clientBuilder(HttpClient.newBuilder())
                            .objectMapper(objectMapper)
                            .build();

                    McpSchema.Implementation clientInfo = new McpSchema.Implementation(
                            properties.getClient().getName() + " - " + connectionName,
                            properties.getClient().getVersion());

                    McpClient.SyncSpec spec = McpClient.sync(transport)
                            .clientInfo(clientInfo)
                            .requestTimeout(properties.getClient().getRequestTimeout());

                    McpSyncClient client = spec.build();
                    mcpSyncClients.add(client);

                    log.info("Successfully created MCP sync client for connection: {}", connectionName);

                } catch (Exception e) {
                    log.error("Failed to create MCP client for connection: {}", connectionName, e);
                    throw new RuntimeException("Failed to create MCP client for connection: " + connectionName, e);
                }
            }
        }

        public List<McpSyncClient> getMcpSyncClients() {
            // Inicializar clientes si no están inicializados
            for (McpSyncClient client : mcpSyncClients) {
                if (!client.isInitialized()) {
                    try {
                        client.initialize();
                        log.info("MCP client initialized on demand: {}", client);
                    } catch (Exception e) {
                        log.warn("Failed to initialize MCP client on demand: {}", e.getMessage());
                    }
                }
            }
            return new ArrayList<>(mcpSyncClients);
        }

        public boolean isInitialized() {
            return initialized;
        }

        public boolean areClientsReady() {
            if (!initialized || mcpSyncClients.isEmpty()) {
                return false;
            }

            for (McpSyncClient client : mcpSyncClients) {
                try {
                    // Intentar obtener las capacidades del servidor para verificar que está listo
                    client.getServerCapabilities();
                } catch (Exception e) {
                    log.debug("Client not ready: {}", e.getMessage());
                    return false;
                }
            }
            return true;
        }

        public void shutdown() {
            log.info("Shutting down {} MCP clients...", mcpSyncClients.size());
            mcpSyncClients.forEach(client -> {
                try {
                    client.close();
                } catch (Exception e) {
                    log.error("Error closing MCP client", e);
                }
            });
            mcpSyncClients.clear();
            initialized = false;
            log.info("MCP clients shutdown completed");
        }
    }
}
