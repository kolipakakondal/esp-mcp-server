package com.esp.mcp.server;

import com.esp.mcp.server.util.FileOperationHelper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public final class McpTools {

    private static final String API_URL = "https://api.kapa.ai/query/v1/projects/bc8d6e33-2168-49fe-889a-def025861f17/chat/";
    private static final String INTEGRATION_ID = "c264a567-2387-498b-9c3f-0528bc1ac5d9";
    private static final String API_KEY = System.getenv("API_KEY");

    public static McpServerFeatures.SyncToolSpecification queryDocsAiTool() {
        final String schema;
        try {
            schema = FileOperationHelper.readResourceAsString("docs-ai-schema.json");
            System.err.println(schema);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read docs-ai-schema.json", e);
        }

        McpSchema.Tool tool = new McpSchema.Tool(
            "esp_docs_ai",
            "Ask technical questions about Espressif chips, ESP-IDF, or related documentation.",
            schema
        );

        return new McpServerFeatures.SyncToolSpecification(
            tool,
            (exchange, arguments) -> {
                String question = arguments.get("query").toString();
                String response;
                boolean isError = false;

                try {
                    response = sendQueryToKapa(question);
                    System.err.println(response);
                } catch (IOException e) {
                    isError = true;
                    response = "Error: " + e.getMessage();
                    System.err.println("Error  " + response);
                    e.printStackTrace(System.err);
                }

                McpSchema.Content content = new McpSchema.TextContent(response);
                System.err.println(content != null ? content.toString() : "null");
                return new McpSchema.CallToolResult(List.of(content), isError);
            }
        );
    }

    private static String sendQueryToKapa(String question) throws IOException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IOException("API_KEY environment variable is not set.");
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-API-KEY", API_KEY);
        conn.setDoOutput(true);

        String jsonInput = String.format("""
            {
              "integration_id": "%s",
              "query": "%s"
            }
            """, INTEGRATION_ID, question.replace("\"", "\\\""));

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "No response from Kapa.ai.";
        }
    }

    public static McpServerFeatures.SyncToolSpecification idfOperationsTool() {
        final String schema;
        try {
            schema = FileOperationHelper.readResourceAsString("idf-ops-schema.json");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read idf-ops-schema.json", e);
        }

        McpSchema.Tool tool = new McpSchema.Tool(
            "idf_operations",
            "Perform build, flash or monitor tasks on ESP-IDF projects",
            schema
        );

        return new McpServerFeatures.SyncToolSpecification(
            tool,
            (exchange, arguments) -> {
                String command = arguments.get("operation").toString();
                String response;
                boolean isError = false;

                switch (command.toLowerCase()) {
                    case "build":
                        response = "build";
                        break;
                    case "flash":
                        response = "flash";
                        break;
                    case "monitor":
                        response = "Opening monitor... (Use `idf.monitor.project` in VSCode)";
                        break;
                    default:
                        response = "Unsupported IDF operation: " + command;
                        isError = true;
                }

                return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(response)), isError);
            }
        );
    }

    public static void addAllTo(McpSyncServer server) {
        try {
            server.addTool(queryDocsAiTool());
            server.addTool(idfOperationsTool());
        } catch (Exception e) {
            System.err.println("Error adding tools");
            e.printStackTrace(System.err);
        }
    }
}
