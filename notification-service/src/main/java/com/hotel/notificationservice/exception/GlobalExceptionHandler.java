package com.hotel.notificationservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorResponse(
                        ex.getMessage(),
                        "BUSINESS_ERROR",
                        ex.getStatus(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(500)
                .body(new ErrorResponse(
                        "Something went wrong",
                        "INTERNAL_SERVER_ERROR",
                        500,
                        request.getRequestURI()
                ));
    }
}