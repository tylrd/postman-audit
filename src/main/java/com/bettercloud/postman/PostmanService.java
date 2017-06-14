package com.bettercloud.postman;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class PostmanService {

    private static final String POSTMAN_API_HEADER = "X-Api-Key";

    private static final String POSTMAN_BASE_URL = "https://api.getpostman.com";

    private static final int DEFAULT_SLEEP = Integer.getInteger("sleep", 2000);

    public static PostmanService get() {
        final String apiKey = System.getProperty("postman.key");
        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalArgumentException(
                    "Must provide postman apiKey with system property -Dpostman.key=API_KEY");
        }
        HttpClient httpClient = HttpClientBuilder.create()
                                                 .addInterceptorFirst(
                                                         (HttpRequestInterceptor) (request, context) -> request.addHeader(
                                                                 POSTMAN_API_HEADER, apiKey))
                                                 .build();
        ObjectMapper mapper = new ObjectMapper();
        return new PostmanService(httpClient, mapper);
    }

    private HttpClient httpClient;
    private ObjectMapper mapper;

    public PostmanService(final HttpClient httpClient, final ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.mapper = objectMapper;
    }

    public JsonNode getCollections() {
        JsonNode rootNode = null;
        try {
            URI uri = new URIBuilder(POSTMAN_BASE_URL).setPath("/collections").build();
            HttpGet collectionsGet = new HttpGet(uri);
            HttpResponse collectionsRawResponse = httpClient.execute(collectionsGet);
            sleep();
            rootNode = mapper.readTree(collectionsRawResponse.getEntity().getContent());
        } catch (URISyntaxException | IOException e) {
            log.error("Error making request to /collections", e);
        }
        return rootNode;
    }

    public JsonNode getCollection(String collectionId) {
        JsonNode rootNode = null;
        try {
            URI uri = new URIBuilder(POSTMAN_BASE_URL).setPath(String.format("/collections/%s", collectionId)).build();
            HttpGet collectionGet = new HttpGet(uri);
            HttpResponse collectionRawResponse = httpClient.execute(collectionGet);
            sleep();
            rootNode = mapper.readTree(collectionRawResponse.getEntity().getContent());
        } catch (URISyntaxException | IOException e) {
            log.error("Error making GET request to /collections/" + collectionId, e);
        }
        return rootNode;
    }

    public void updateCollection(String collectionId, JsonNode rootNode) {
        try {
            ObjectWriter writer = mapper.writer();
            URI uri = new URIBuilder(POSTMAN_BASE_URL).setPath(String.format("/collections/%s", collectionId)).build();
            HttpEntity entity = new ByteArrayEntity(writer.writeValueAsBytes(rootNode));
            HttpPut httpPut = new HttpPut(uri);
            httpPut.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPut);
            sleep();
            if (response.getStatusLine().getStatusCode() / 100 == 2) {
                log.info("Successfully updated hmac script for collection " + collectionId);
            } else {
                log.error("Error updating collection: " + collectionId + "\n" + EntityUtils.toString(
                        response.getEntity()));
            }
        } catch (URISyntaxException | IOException e) {
            log.error("Error making PUT request to /collections/" + collectionId, e);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(DEFAULT_SLEEP);
        } catch (InterruptedException e) {
            log.error("Error sleeping between requests", e);
        }
    }

}
