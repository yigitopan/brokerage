package com.opan.brokerageapi.exceptions;

public class InvalidIBANException extends RuntimeException {
    public InvalidIBANException(String message) {
        super(message);
    }
}