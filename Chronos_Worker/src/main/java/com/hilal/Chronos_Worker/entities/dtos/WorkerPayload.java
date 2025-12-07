package com.hilal.Chronos_Worker.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerPayload {
    private String url;
    private String method;
    private Map<String, String> headers;
    private Map<String, Object> body;
    private int timeoutMs;

}
