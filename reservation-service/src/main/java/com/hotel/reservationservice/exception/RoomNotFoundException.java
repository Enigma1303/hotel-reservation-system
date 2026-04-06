package com.hotel.reservationservice.exception;

public class RoomNotFoundException extends BusinessException {
    public RoomNotFoundException(Long id) {
        super("Room not found with id: " + id, 404);
    }
}