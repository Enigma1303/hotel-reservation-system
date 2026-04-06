package com.hotel.customerservice.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String message;
    private String error;
    private int status;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse(String message, String error, int status, String path) {
        this.message = message;
        this.error = error;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() { return message; }
    public String getError() { return error; }
    public int getStatus() { return status; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
}