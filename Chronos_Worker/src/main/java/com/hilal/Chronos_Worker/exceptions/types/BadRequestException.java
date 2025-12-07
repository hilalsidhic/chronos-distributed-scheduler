package com.hilal.Chronos_Worker.exceptions.types;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) {
        super(msg);
    }
}