package com.bettercloud.postman;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

@Slf4j
public class UpdatePrequest extends BasePostmanScript implements Runnable {

    public static void main(String[] args) {
        PostmanService postmanService = PostmanService.get();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        String scriptName = System.getProperty("prerequest.filename");
        if (scriptName == null) {
            throw new IllegalArgumentException("Must provide system property -Dprerequest.filename=");
        }
        ArrayNode script = buildScript(scriptName, loader.getResourceAsStream(scriptName));
        if (script != null) {
            UpdatePrequest updatePrerequestScript = new UpdatePrequest(postmanService, script);
            updatePrerequestScript.run();
        }
    }

    private static ArrayNode buildScript(String scriptName, InputStream is) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode node = mapper.createArrayNode();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String line = in.readLine();
            node.add(line);
        } catch (IOException e) {
            log.warn("Error creating prerequest script from {} file", scriptName, e);
        }
        return node;
    }

    private final ArrayNode script;

    private UpdatePrequest(PostmanService postmanService, ArrayNode script) {
        super(postmanService);
        this.script = script;
    }

    @Override
    public void run() {
        JsonNode rootNode = postmanService.getCollections();
        processCollections(rootNode, node -> updateScriptForAllRequests(node.get("uid").textValue()));
    }

    private void updateScriptForAllRequests(String collectionId) {
        JsonNode rootNode = postmanService.getCollection(collectionId);
        processItems(rootNode, this::updateItemNode);
        postmanService.updateCollection(collectionId, rootNode);
    }

    private void updateItemNode(JsonNode itemNode) {
        if (!itemNode.has("item") && itemNode.has("event")) {
            Iterator<JsonNode> eventIterator = itemNode.get("event").elements();
            while (eventIterator.hasNext()) {
                JsonNode eventNode = eventIterator.next();
                if (eventNode.has("listen")
                    && eventNode.get("listen").textValue().equals("prerequest")
                    && eventNode.has("script")) {
                    ObjectNode scriptNode = (ObjectNode) eventNode.get("script");
                    if (scriptNode.has("exec")) {
                        scriptNode.putArray("exec").removeAll().addAll(script);
                    }
                }
            }
        }
    }

}
