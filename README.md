## Overview

The esp-mcp-server is a lightweight, embeddable HTTP server that implements the Model Context Protocol (MCP) for Espressif’s AI-assisted development tool

## How to Use

Step #1: You can start the server using Docker:
```
docker run -p 8080:8080 kolipakakondal/esp-mcp-server:latest
```
This will launch the server and expose it on http://127.0.0.1:8080.

Step #2: Integration in IDE's (VSCode/Cursor)

Configure your tools to use the MCP server with the following structure:

Cursor IDE

```
"esp-mcp-server": {
      "url": " http://127.0.0.1:8080/mcp",
      "env": {
        "API_KEY": "<API_KEY_HERE>"
      }
```

VSCode
```
  "esp-mcp-server": {
         "type": "sse",
         "url": "http://127.0.0.1:8080/mcp",
         "env": {
            "API_KEY": "<API_KEY_HERE>"
                }
            }
```

## Supported Tools
- esp_docs_ai: Ask technical questions about espressif chips, esp-idf and related documents. 
Parameters: query
