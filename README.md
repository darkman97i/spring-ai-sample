# MCP Test Environment
This directory contains a test environment for the MCP (Model Context Protocol) using SSE (Server-Sent Events).

This example uses:
* spring-ai-boom 1.0.0
* spring-ai-starter-mcp-server-webmvc
* spring boot 3.5.3
* an SSE endpoint configuration is established together with the application context
* ***In the client's application.properties file, a valid OpenAI API key must be set***
* ***In the server-client's application.properties file, a valid OpenAI API key must be set***


## Structure

- `mcp-server/` - MCP server that exposes tools via SSE
- `mcp-client/` - MCP client that consumes tools from the server
- `mcp-server-client/` - MCP server that exposes tools via SSE and client that consumes tools from the server

## MCP Server

### Configuration
- Port: 8080
- Context path: `/openkm`
- SSE Endpoint: `/openkm/sse`
- Message Endpoint: `/openkm/mcp/message`

### Available Tools
- `getSystemDate` - Returns current system date and time
- `getTimestamp` - Returns current timestamp in milliseconds
- `getAppInfo` - Returns basic information about openkm

### Execution
```bash
cd mcp-server
mvn spring-boot:run
```

## MCP Client

### Configuration
- Port: 8082

### Functionality
The client establishes an SSE connection with the server, initializes the MCP session, and makes calls to the available tools.

### Execution
```bash
cd mcp-client
mvn spring-boot:run
```

## MCP Server Client together

### Configuration
- Port: 8080
- Context path: `/openkm`
- SSE Endpoint: `/openkm/sse`
- Message Endpoint: `/openkm/mcp/message`

### Available Tools
- `getSystemDate` - Returns current system date and time
- `getTimestamp` - Returns current timestamp in milliseconds
- `getAppInfo` - Returns basic information about openkm

### Execution Server and client separated
```bash
cd mcp-server
mvn spring-boot:run

## System Testing

1. Start the server:
```bash
cd mcp-server
mvn spring-boot:run
```

2. In another terminal, start the client:
```bash
cd mcp-client
mvn spring-boot:run
```

### Execution Server and client together
```bash
cd mcp-server-client
mvn spring-boot:run
```

## Logs

Both applications are configured with DEBUG logging to view the MCP message exchange and SSE connection establishment.

