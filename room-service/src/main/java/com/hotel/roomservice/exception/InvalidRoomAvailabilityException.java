package com.hotel.roomservice.exception;

public class InvalidRoomAvailabilityException extends BusinessException {
    public InvalidRoomAvailabilityException() {
        super("Availability cannot be null", 400);
    }
}