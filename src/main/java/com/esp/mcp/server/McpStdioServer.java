package com.esp.mcp.server;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

public class McpStdioServer {

    private static final String SERVER_NAME = "esp-mcp-server";
    private static final String SERVER_VERSION = "0.0.1";
    private McpSyncServer server;

    /**
     * Initialize the STDIO MCP server
     */
    private void initialize() {
        McpSchema.ServerCapabilities serverCapabilities = McpSchema.ServerCapabilities.builder()
            .tools(true)
            .prompts(true)
            .resources(false, false) // no resources used for docs-ai yet
            .build();

        server = McpServer.sync(new StdioServerTransportProvider())
            .serverInfo(SERVER_NAME, SERVER_VERSION)
            .capabilities(serverCapabilities)
            .build();

        // STDIO mode log message
        System.err.println(SERVER_NAME + " " + SERVER_VERSION + " initialized in STDIO mode");
    }

    /**
     * Main entry point for the ESP MCP STDIO MCP server.
     */
    public static void main(String[] args) {
        McpStdioServer mcpStdioServer = new McpStdioServer();
        mcpStdioServer.initialize();

        McpPrompts.addAllTo(mcpStdioServer.server);
        McpTools.addAllTo(mcpStdioServer.server);
    }
}
