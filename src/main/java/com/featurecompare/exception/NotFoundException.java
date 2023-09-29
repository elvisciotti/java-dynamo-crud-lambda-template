package com.featurecompare.exception;

public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        super("Item not found");
    }
}
