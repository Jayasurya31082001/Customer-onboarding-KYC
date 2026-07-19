package com.example.documentservice.common.exception;

public class InvalidDocumentException extends RuntimeException {

    public InvalidDocumentException(String message) {
        super(message);
    }
}
