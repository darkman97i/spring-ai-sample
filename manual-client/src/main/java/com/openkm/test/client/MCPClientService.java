package com.openkm.test.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MCPClientService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl = "http://localhost:8081/test-mcp";

    public MCPClientService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    public String performSelfTest() {
        StringBuilder result = new StringBuilder();
        result.append("=== MCP CLIENT SELF-TEST ===\n\n");

        try {
            // Paso 1: Establecer conexión SSE
            result.append("1. ESTABLISHING SSE CONNECTION:\n");
            String sessionId = establishSSEConnection();
            if (sessionId != null) {
                result.append("✅ SSE connection established, sessionId: ").append(sessionId).append("\n\n");
            } else {
                result.append("❌ Failed to establish SSE connection\n\n");
                return result.toString();
            }

            // Paso 2: Inicializar sesión MCP
            result.append("2. INITIALIZING MCP SESSION:\n");
            String initResult = initializeMCPSession(sessionId);
            result.append(initResult).append("\n\n");

            // Paso 3: Listar herramientas disponibles
            result.append("3. LISTING AVAILABLE TOOLS:\n");
            String toolsListResult = listAvailableTools(sessionId);
            result.append(toolsListResult).append("\n\n");

            // Paso 4: Llamar herramienta getSystemDate
            result.append("4. CALLING getSystemDate TOOL:\n");
            String dateResult = callTool(sessionId, "getSystemDate");
            result.append(dateResult).append("\n\n");

            // Paso 5: Llamar herramienta getTimestamp
            result.append("5. CALLING getTimestamp TOOL:\n");
            String timestampResult = callTool(sessionId, "getTimestamp");
            result.append(timestampResult).append("\n\n");

        } catch (Exception e) {
            result.append("❌ Error during self-test: ").append(e.getMessage()).append("\n");
            log.error("Error during self-test", e);
        }

        return result.toString();
    }

    private String establishSSEConnection() {
        try {
            log.info("Attempting to establish SSE connection at: {}/mcp/sse", baseUrl);
            
            URL url = new URL(baseUrl + "/mcp/sse");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");
            
            log.debug("SSE connection opened, reading first event...");
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("SSE line: {}", line);
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (data.startsWith("/mcp/message?sessionId=")) {
                            String sessionId = data.substring(data.indexOf("sessionId=") + 10);
                            log.info("Extracted sessionId: {}", sessionId);
                            return sessionId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to establish SSE connection", e);
        }
        return null;
    }

    private String initializeMCPSession(String sessionId) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", "client-test-init");
            request.put("method", "initialize");
            
            Map<String, Object> params = new HashMap<>();
            params.put("protocolVersion", "2024-11-05");
            
            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("name", "mcp-test-client");
            clientInfo.put("version", "1.0.0");
            params.put("clientInfo", clientInfo);
            
            params.put("capabilities", new HashMap<>());
            request.put("params", params);

            String response = sendMCPRequest(sessionId, request);
            return "✅ Initialize successful: " + response;
        } catch (Exception e) {
            return "❌ Initialize failed: " + e.getMessage();
        }
    }

    private String listAvailableTools(String sessionId) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", "client-test-tools-list");
            request.put("method", "tools/list");

            String response = sendMCPRequest(sessionId, request);
            return "✅ Tools list successful: " + response;
        } catch (Exception e) {
            return "❌ Tools list failed: " + e.getMessage();
        }
    }

    private String callTool(String sessionId, String toolName) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", "client-test-tools-call-" + toolName);
            request.put("method", "tools/call");
            
            Map<String, Object> params = new HashMap<>();
            params.put("name", toolName);
            request.put("params", params);

            String response = sendMCPRequest(sessionId, request);
            return "✅ Tool call successful: " + response;
        } catch (Exception e) {
            return "❌ Tool call failed: " + e.getMessage();
        }
    }

    private String sendMCPRequest(String sessionId, Map<String, Object> request) throws IOException {
        String url = baseUrl + "/mcp/message?sessionId=" + sessionId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        log.debug("Sending MCP request to {}: {}", url, objectMapper.writeValueAsString(request));
        
        String response = restTemplate.postForObject(url, entity, String.class);
        
        log.debug("Received MCP response: {}", response);
        
        return response != null ? response : "No response";
    }
}
