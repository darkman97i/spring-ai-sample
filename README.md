# MCP Test Environment

This directory contains a test environment for the MCP (Model Context Protocol) using SSE (Server-Sent Events).

## Structure

- `mcp-server/` - MCP server that exposes tools via SSE
- `mcp-client/` - MCP client that consumes tools from the server

## MCP Server

### Configuration
- Port: 8081
- Context path: `/test-mcp`
- SSE Endpoint: `/test-mcp/mcp/sse`
- Message Endpoint: `/test-mcp/mcp/message`

### Available Tools
- `getSystemDate` - Returns current system date and time
- `getTimestamp` - Returns current timestamp in milliseconds

### Execution
```bash
cd mcp-server
mvn spring-boot:run
```

## MCP Client

### Configuration
- Port: 8082
- Context path: `/test-client`
- Test Endpoint: `/test-client/test`

### Functionality
The client establishes an SSE connection with the server, initializes the MCP session, and makes calls to the available tools.

### Execution
```bash
cd mcp-client
mvn spring-boot:run
```

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

3. Test the communication:
```bash
curl http://localhost:8082/test-client/test
```

## Logs

Both applications are configured with DEBUG logging to view the MCP message exchange and SSE connection establishment.
