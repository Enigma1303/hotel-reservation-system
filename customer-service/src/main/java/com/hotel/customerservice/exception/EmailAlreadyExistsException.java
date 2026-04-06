package com.hotel.customerservice.exception;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email, 409);
    }
}