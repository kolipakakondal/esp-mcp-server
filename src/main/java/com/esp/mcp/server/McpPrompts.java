package com.esp.mcp.server;

import com.esp.mcp.server.util.FileOperationHelper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class McpPrompts {

    public static McpServerFeatures.SyncPromptSpecification queryEspDocs() {
        McpSchema.PromptArgument query = new McpSchema.PromptArgument(
            "query", "The question or query to ask about the espressif documentation.", true);

        McpSchema.Prompt prompt = new McpSchema.Prompt(
            "esp_docs_query",
            "Query the espressif documentation using the Docs AI tool.",
            List.of(query)
        );

        return new McpServerFeatures.SyncPromptSpecification(
            prompt,
            (exchange, request) -> {
                Map<String, Object> arguments = request.arguments();
                String question = arguments.get("query").toString();

                final String schema;
                try {
                    schema = FileOperationHelper.readResourceAsString("docs-ai-schema.json");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load docs-ai-schema.json", e);
                }

                McpSchema.TextContent content = new McpSchema.TextContent(schema + "\n\n" + question);
                McpSchema.PromptMessage message = new McpSchema.PromptMessage(McpSchema.Role.USER, content);

                return new McpSchema.GetPromptResult(prompt.description(), List.of(message));
            }
        );
    }

    /**
     * Add all prompts to the MCP server.
     *
     * @param server The MCP server to add prompts to.
     */
    public static void addAllTo(McpSyncServer server) {
        server.addPrompt(queryEspDocs());
    }

}
