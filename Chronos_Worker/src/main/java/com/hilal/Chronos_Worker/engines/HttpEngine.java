package com.hilal.Chronos_Worker.engines;

import com.hilal.Chronos_Worker.entities.dtos.HttpResult;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;

// --- CORRECTED IMPORTS START ---
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntityContainer; // Was missing
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
// --- CORRECTED IMPORTS END ---

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class HttpEngine {

    private final CloseableHttpClient client;

    public HttpEngine() {
        this.client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(5))
                        .setResponseTimeout(Timeout.DISABLED)
                        .build())
                .build();
    }

    public HttpResult execute(WorkerPayload payload) {

        String method = payload.getMethod().toUpperCase();

        ClassicHttpRequest request;

        // CREATE REQUEST OBJECT
        switch (method) {
            case "GET":
                request = new HttpGet(payload.getUrl());
                break;
            case "DELETE":
                request = new HttpDelete(payload.getUrl());
                break;
            case "HEAD":
                request = new HttpHead(payload.getUrl());
                break;
            case "PUT":
                request = new HttpPut(payload.getUrl());
                break;
            case "PATCH":
                request = new HttpPatch(payload.getUrl());
                break;
            case "POST":
            default:
                request = new HttpPost(payload.getUrl());
                break;
        }

        // HEADERS
        if (payload.getHeaders() != null) {
            payload.getHeaders().forEach(request::addHeader);
        }

        // BODY (only for POST/PUT/PATCH)
        // This requires the HttpEntityContainer import added above
        if (request instanceof HttpEntityContainer) {
            String bodyString = payload.getBody() != null
                    ? payload.getBody().toString()
                    : "";

            ((HttpEntityContainer) request).setEntity(
                    new StringEntity(bodyString, ContentType.APPLICATION_JSON)
            );
        }

        long start = System.currentTimeMillis();

        try (CloseableHttpResponse response = client.execute(request)) {

            long duration = System.currentTimeMillis() - start;

            String responseBody = "";

            // Check if entity exists before reading
            if (response.getEntity() != null) {
                // Using StandardCharsets directly inside the constructor
                responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            }

            return HttpResult.builder()
                    .statusCode(response.getCode())
                    .responseBody(responseBody)
                    .durationMs(duration)
                    .success(response.getCode() < 500)
                    .build();

        } catch (IOException ex) {
            ex.printStackTrace();
            return HttpResult.builder()
                    .success(false)
                    .error("IO Error: " + ex.getMessage())
                    .durationMs(0)
                    .build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResult.builder()
                    .success(false)
                    .error(ex.getMessage())
                    .durationMs(0)
                    .build();
        }
    }
}