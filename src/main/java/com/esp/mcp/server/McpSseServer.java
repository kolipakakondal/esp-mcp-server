package com.esp.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McpSseServer {

    private static final Logger logger = LoggerFactory.getLogger(McpSseServer.class);
    private static final String SERVER_NAME = "esp-mcp-server";
    private static final String SERVER_VERSION = "0.0.1";

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String MSG_ENDPOINT = "/mcp/message";
    private static final String SSE_ENDPOINT = "/mcp";

    private McpSyncServer server;

    private void initialize() {
        McpSchema.ServerCapabilities serverCapabilities = McpSchema.ServerCapabilities.builder()
            .tools(true)
            .prompts(true)
            .resources(false, false)
            .build();

        HttpServletSseServerTransportProvider transport = new HttpServletSseServerTransportProvider(
            JSON, MSG_ENDPOINT, SSE_ENDPOINT
        );
        server = McpServer.sync(transport)
            .serverInfo(SERVER_NAME, SERVER_VERSION)
            .capabilities(serverCapabilities)
            .build();

        McpTools.addAllTo(server);

        // Start the HTTP server
        startHttpServer(transport);
    }

    /**
     * Start the HTTP server with Jetty.
     */
    private void startHttpServer(HttpServletSseServerTransportProvider transport) {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(transport);
        servletContextHandler.addServlet(servletHolder, "/*");

        Server httpserver = new Server(8080);
        httpserver.setHandler(servletContextHandler);

        try {
            httpserver.start();
            logger.info("Jetty-based HTTP server started on http://127.0.0.1:8080");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Shutting down HTTP server");
                    httpserver.stop();
                    server.close();
                } catch (Exception e) {
                    logger.error("Error stopping HTTP server", e);
                }
            }));

            // Wait for the HTTP server to stop
            httpserver.join();
        } catch (Exception e) {
            logger.error("Error starting HTTP server on http://127.0.0.1:8080", e);
            server.close();
        }
    }

    /**
     * Main entry point for the HTTP SSE MCP server.
     */
    public static void main(String[] args) {
        // Initialize MCP server
        McpSseServer mcpSseServer = new McpSseServer();
        mcpSseServer.initialize();
    }

}
