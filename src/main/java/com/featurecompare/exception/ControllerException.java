package com.featurecompare.exception;

public class ControllerException extends Exception {
    int code;

    public ControllerException(String message, int httpCode) {
        super(message);
        this.code = httpCode;
    }

    public int getHTTPCode() {
        return code;
    }
}
