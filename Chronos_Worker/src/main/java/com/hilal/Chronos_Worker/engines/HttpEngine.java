package com.hilal.Chronos_Worker.engines;

import com.hilal.Chronos_Worker.entities.dtos.HttpResult;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class HttpEngine {

    private final CloseableHttpClient client;

    // Constructor Injection (Spring picks up the Bean from HttpClientConfig)
    public HttpEngine(CloseableHttpClient client) {
        this.client = client;
    }

    public HttpResult execute(WorkerPayload payload) {
        String method = payload.getMethod().toUpperCase();
        ClassicHttpRequest request = createRequest(method, payload.getUrl());

        // 1. ADD HEADERS
        if (payload.getHeaders() != null) {
            payload.getHeaders().forEach(request::addHeader);
        }

        // 2. ADD BODY (For POST, PUT, PATCH)
        if (request instanceof HttpEntityContainer && payload.getBody() != null) {
            String bodyString = payload.getBody().toString();
            // Defaulting to JSON. If your payload supports other types, pass it in WorkerPayload
            ((HttpEntityContainer) request).setEntity(
                    new StringEntity(bodyString, ContentType.APPLICATION_JSON)
            );
        }

        long start = System.currentTimeMillis();

        try (CloseableHttpResponse response = client.execute(request)) {

            long duration = System.currentTimeMillis() - start;
            String responseBody = "";
            HttpEntity entity = response.getEntity();

            // 3. SAFE RESPONSE READING
            if (entity != null) {
                // EntityUtils handles charset detection and stream closing automatically
                responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                // SAFETY: Truncate very large responses to prevent OutOfMemoryError
                if (responseBody != null && responseBody.length() > 100_000) {
                    responseBody = responseBody.substring(0, 100_000) + " ...[TRUNCATED]";
                }
            }

            int code = response.getCode();

            return HttpResult.builder()
                    .statusCode(code)
                    .responseBody(responseBody)
                    .durationMs(duration)
                    // Success is strictly 2xx. 404 or 500 are considered failed jobs.
                    .success(code >= 200 && code < 300)
                    .build();

        } catch (IOException ex) {
            // Network failures (Connection refused, Timeout, DNS failure)
            return HttpResult.builder()
                    .statusCode(0) // 0 indicates request didn't complete HTTP handshake
                    .success(false)
                    .error("Network Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage())
                    .durationMs(System.currentTimeMillis() - start)
                    .build();

        } catch (Exception ex) {
            // Unexpected application errors
            ex.printStackTrace();
            return HttpResult.builder()
                    .statusCode(0)
                    .success(false)
                    .error("Internal Error: " + ex.getMessage())
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    /**
     * Helper to map string method to Apache HTTP Request object
     */
    private ClassicHttpRequest createRequest(String method, String url) {
        switch (method) {
            case "GET":    return new HttpGet(url);
            case "DELETE": return new HttpDelete(url);
            case "HEAD":   return new HttpHead(url);
            case "PUT":    return new HttpPut(url);
            case "PATCH":  return new HttpPatch(url);
            case "POST":   return new HttpPost(url);
            default:       return new HttpPost(url); // Default to POST or throw exception
        }
    }
}