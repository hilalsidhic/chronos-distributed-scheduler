package com.hilal.Chronos_Worker.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpResult {
    private int statusCode;
    private String responseBody;
    private long durationMs;
    private boolean success;
    private String error;
}
