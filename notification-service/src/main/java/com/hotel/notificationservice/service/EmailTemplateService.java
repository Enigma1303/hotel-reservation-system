package com.hotel.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    public String reservationConfirmed(Long reservationId, Long roomId) {
        String template = """
                ==========================================
                To: Customer
                Subject: Reservation Confirmed – Booking #%d
                
                Hi Customer,
                Your reservation has been successfully confirmed.
                
                Reservation Details:
                Reservation ID : %d
                Room ID        : %d
                Status         : CONFIRMED
                
                Thank you for choosing our service.
                — Hotel Reservation System
                ==========================================
                """.formatted(reservationId, reservationId, roomId);
        log.info("Generated reservation confirmation email for reservationId: {}", reservationId);
        return template;
    }

    public String paymentSuccess(Long reservationId) {
        String template = """
                ==========================================
                To: Customer
                Subject: Payment Successful – Reservation #%d
                
                Hi Customer,
                Your payment has been successfully processed.
                
                Reservation ID : %d
                Payment Status : SUCCESS
                Your booking is now confirmed.
                — Hotel Reservation System
                ==========================================
                """.formatted(reservationId, reservationId);
        log.info("Generated payment success email for reservationId: {}", reservationId);
        return template;
    }

    public String paymentFailed(Long reservationId) {
        String template = """
                ==========================================
                To: Customer
                Subject: Payment Failed – Reservation #%d
                
                Hi Customer,
                We were unable to process your payment for the reservation.
                
                Reservation ID : %d
                Payment Status : FAILED
                Please try again to complete your booking.
                — Hotel Reservation System
                ==========================================
                """.formatted(reservationId, reservationId);
        log.info("Generated payment failed email for reservationId: {}", reservationId);
        return template;
    }

    public String reservationCancelled(Long reservationId) {
        String template = """
                ==========================================
                To: Customer
                Subject: Reservation Cancelled – Booking #%d
                
                Hi Customer,
                Your reservation has been successfully cancelled.
                
                Reservation ID : %d
                Status         : CANCELLED
                If this was not intended, please create a new reservation.
                — Hotel Reservation System
                ==========================================
                """.formatted(reservationId, reservationId);
        log.info("Generated reservation cancelled email for reservationId: {}", reservationId);
        return template;
    }

    public String notificationRetryFailed(Long notificationId, int retryCount) {
        String template = """
                ==========================================
                To: Admin
                Subject: Notification Delivery Failed – Notification #%d
                
                Hi Admin,
                A notification has failed to be delivered even after multiple retry attempts.
                
                Notification ID : %d
                Status          : FAILED
                Retry Count     : %d
                Manual intervention may be required.
                — Hotel Reservation System
                ==========================================
                """.formatted(notificationId, notificationId, retryCount);
        log.warn("Generated retry failure email for notificationId: {}", notificationId);
        return template;
    }

    public String notificationRetrySuccess(Long notificationId) {
        String template = """
                ==========================================
                To: Admin
                Subject: Notification Successfully Delivered After Retry
                
                Hi Admin,
                A previously failed notification has been successfully delivered after retry.
                
                Notification ID : %d
                Final Status    : SENT
                — Hotel Reservation System
                ==========================================
                """.formatted(notificationId);
        log.info("Generated retry success email for notificationId: {}", notificationId);
        return template;
    }
}