package com.melancholia.manager.core;

public class ApiResponse {

    private final String message;
    private final int statusCode;
    private final Object data;

    public ApiResponse(String message, int statusCode, Object data) {
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Object getData() {
        return data;
    }

}
