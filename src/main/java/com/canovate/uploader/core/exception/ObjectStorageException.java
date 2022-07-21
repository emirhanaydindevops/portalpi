package com.canovate.uploader.core.exception;

public class ObjectStorageException extends Exception {
    private String message;

    public ObjectStorageException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
