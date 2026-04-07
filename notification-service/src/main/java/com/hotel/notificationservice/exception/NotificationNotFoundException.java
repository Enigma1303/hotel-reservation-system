package com.hotel.notificationservice.exception;

public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException(Long id) {
        super("Notification not found with id: " + id, 404);
    }
}