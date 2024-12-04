package com.opan.brokerageapi.exceptions;

public class InvalidOrderSideException extends RuntimeException {
    public InvalidOrderSideException(String message) {
        super(message);
    }
}