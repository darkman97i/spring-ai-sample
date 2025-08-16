package com.openkm.test.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "mcp.manual")
public class ManualMcpClientProperties {

    public static final String CONFIG_PREFIX = "mcp.manual";

    private Client client = new Client();
    private Map<String, SseConnection> connections = new HashMap<>();

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Map<String, SseConnection> getConnections() {
        return connections;
    }

    public void setConnections(Map<String, SseConnection> connections) {
        this.connections = connections;
    }

    public static class Client {
        private boolean enabled = true;
        private String type = "sync";
        private boolean initialized = false;
        private String name = "openkm-mcp-client";
        private String version = "1.0.0";
        private Duration requestTimeout = Duration.ofSeconds(20);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isInitialized() {
            return initialized;
        }

        public void setInitialized(boolean initialized) {
            this.initialized = initialized;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Duration getRequestTimeout() {
            return requestTimeout;
        }

        public void setRequestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
        }
    }

    public static class SseConnection {
        private String url;
        private String sseEndpoint = "/sse";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSseEndpoint() {
            return sseEndpoint;
        }

        public void setSseEndpoint(String sseEndpoint) {
            this.sseEndpoint = sseEndpoint;
        }
    }
}
