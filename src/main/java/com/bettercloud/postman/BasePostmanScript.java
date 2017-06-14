package com.bettercloud.postman;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.function.Consumer;

@Slf4j
public abstract class BasePostmanScript {

    protected PostmanService postmanService;

    public BasePostmanScript(final PostmanService postmanService) {
        this.postmanService = postmanService;
    }

    protected void processCollections(JsonNode rootNode, Consumer<JsonNode> f) {
        if (rootNode != null && rootNode.has("collections") && rootNode.get("collections").isArray()) {
            Iterator<JsonNode> collections = rootNode.get("collections").elements();
            while (collections.hasNext()) {
                f.accept(collections.next());
            }
        }
    }

    protected void processItems(JsonNode rootNode, Consumer<JsonNode> f) {
        if (rootNode != null && rootNode.has("collection") && rootNode.get("collection").has("item") && rootNode.get(
                "collection").get("item").isArray()) {
            log.info("Processing collection {}", rootNode.get("collection").get("info").get("name").textValue());
            Iterator<JsonNode> itemIterator = rootNode.get("collection").get("item").elements();
            processItems(itemIterator, f);
        }
    }

    private void processItems(Iterator<JsonNode> itemIterator, Consumer<JsonNode> f) {
        while (itemIterator.hasNext()) {
            JsonNode itemNode = itemIterator.next();
            if (!itemNode.has("item") && itemNode.has("event")) {
                f.accept(itemNode);
            }
            if (itemNode.has("item") && itemNode.get("item").isArray()) {
                processItems(itemNode.get("item").elements(), f);
            }
        }
    }

}
