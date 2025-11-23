package com.hilal.Chronos_Scheduler.exceptions.types;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) {
        super(msg);
    }
}