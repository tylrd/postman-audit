package com.bettercloud.postman;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class AuditPostman extends BasePostmanScript implements Runnable {

    public static void main(String[] args) {
        PostmanService postmanService = PostmanService.get();
        String sensitiveStringsCSV = System.getProperty("sensitive.strings", "");
        List<String> sensitiveStrings = Arrays.asList(sensitiveStringsCSV.trim().split(","));
        AuditPostman auditHmacScript = new AuditPostman(postmanService, sensitiveStrings);
        auditHmacScript.run();
    }

    private final List<String> sensitiveStrings;

    public AuditPostman(final PostmanService postmanService, final List<String> sensitiveStrings) {
        super(postmanService);
        this.sensitiveStrings = sensitiveStrings;
    }

    @Override
    public void run() {
        JsonNode rootNode = postmanService.getCollections();
        processCollections(rootNode, node -> audit(node.get("uid").textValue()));
    }

    private void audit(String collectionId) {
        JsonNode rootNode = postmanService.getCollection(collectionId);
        processItems(rootNode, this::auditItemNode);
    }

    private void auditItemNode(JsonNode itemNode) {
        String requestName = itemNode.get("name").textValue();
        if (itemNode.has("request")) {
            JsonNode requestNode = itemNode.get("request");
            if (requestNode.has("header")) {
                auditHeaders(requestNode.get("header"), requestName);
            }
            if (requestNode.has("body") && !requestNode.get("body").isNull()) {
                auditRequestBody(requestNode.get("body"), requestName);
            }
        }
        if (itemNode.has("event")) {
            Iterator<JsonNode> eventIterator = itemNode.get("event").elements();
            while (eventIterator.hasNext()) {
                JsonNode eventNode = eventIterator.next();
                if (eventNode.has("listen")
                    && eventNode.get("listen").textValue().equals("prerequest")
                    && eventNode.has("script")) {
                    JsonNode scriptNode = eventNode.get("script");
                    if (scriptNode.has("exec")) {
                        auditPrerequest(scriptNode.get("exec"), requestName);
                    }
                }
            }
        }
    }

    private void auditHeaders(JsonNode headerNode, String requestName) {
        Iterator<JsonNode> headerElements = headerNode.elements();
        while (headerElements.hasNext()) {
            JsonNode headerElement = headerElements.next();
            String value = headerElement.get("value").textValue();
            auditValue(value, requestName);
        }
    }

    private void auditRequestBody(JsonNode bodyNode, String requestName) {
        if (bodyNode.has("mode")) {
            String mode = bodyNode.get("mode").textValue();
            if ("raw".equals(mode)) {
                String rawValue = bodyNode.get("raw").textValue();
                auditValue(rawValue, requestName);
            }
        }
    }

    private void auditPrerequest(JsonNode execNode, String requestName) {
        Iterator<JsonNode> lines = execNode.elements();
        while (lines.hasNext()) {
            JsonNode execLine = lines.next();
            auditValue(execLine.textValue(), requestName);
        }
    }

    private void auditValue(String value, String requestName) {
        for (String sensitiveString : sensitiveStrings) {
            if (value.contains(sensitiveString)) {
                log.warn("Sensitive value found in postman request: " + requestName);
            }
        }
    }

}
