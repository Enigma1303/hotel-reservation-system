package com.hotel.notificationservice.exception;

public class InvalidNotificationStateException extends BusinessException {

    public InvalidNotificationStateException(Long id, String currentState) {
        super("Notification " + id + " is not in FAILED state. Current state: " + currentState, 409);
    }
}