package com.hilal.Chronos_Worker.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    @Value("${worker.http.max.connections:200}")
    private int maxConnections;

    @Value("${worker.http.max.per_route:20}")
    private int maxPerRoute;

    @Value("${worker.http.timeout.connect:5}")
    private int connectTimeout;

    @Value("${worker.http.timeout.request:30}")
    private int requestTimeout;

    @Value("${worker.http.timeout.connection_request:5}")
    private int ConnectionRequestTimeout;

    @Value("${worker.http.timeout.idle_connections:5}")
    private int evictIdleTimeout;

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        // 1. POOLING CONNECTION MANAGER
        // This allows multiple threads to reuse connections instead of opening/closing them constantly.
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        // Max concurrent connections in total
        connectionManager.setMaxTotal(maxConnections);
        // Max concurrent connections per route (e.g., max 20 connections to google.com at once)
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);

        // 2. TIMEOUT CONFIGURATION
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(connectTimeout, TimeUnit.SECONDS))           // Time to establish connection
                .setResponseTimeout(Timeout.of(requestTimeout, TimeUnit.SECONDS))         // Time to wait for data (prevents hanging threads)
                .setConnectionRequestTimeout(Timeout.of(ConnectionRequestTimeout, TimeUnit.SECONDS)) // Time to wait for a connection from the pool
                .build();

        // 3. BUILD CLIENT
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(Timeout.of(evictIdleTimeout, TimeUnit.SECONDS))
                .disableAutomaticRetries()
                .build();
    }
}