package com.hotel.notificationservice.service;

import com.hotel.notificationservice.dto.EmailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    private static final String ADMIN_EMAIL = "notificationtester612@gmail.com";

    // ---------------- CUSTOMER EMAILS ----------------

    public EmailDto reservationConfirmed(Long reservationId, Long roomId) {
        String subject = "Reservation Received – Booking #" + reservationId;
        String body = """
                Hi Customer,
                Your reservation has been successfully received and is currently pending confirmation.
                
                Reservation Details:
                Reservation ID : %d
                Room ID        : %d
                Status         : PENDING
                
                You will receive another email once payment is processed.
                Thank you for choosing our service. We look forward to hosting you.
                — Hotel Reservation System
                """.formatted(reservationId, roomId);

        log.info("Generated reservation confirmation email for reservationId: {}", reservationId);

        return new EmailDto(null, subject, body);
    }

    public EmailDto paymentSuccess(Long reservationId) {
        String subject = "Payment Successful – Reservation #" + reservationId;
        String body = """
                Hi Customer,
                Your payment has been successfully processed. 
                Your reservation is now confirmed.

                Reservation Details:
                Reservation ID : %d
                Status         : CONFIRMED
                Payment Status : SUCCESS
                Your booking is now confirmed.
                — Hotel Reservation System
                """.formatted(reservationId);

        log.info("Generated payment success email for reservationId: {}", reservationId);

        return new EmailDto(null, subject, body);
    }

    public EmailDto paymentFailed(Long reservationId) {
        String subject = "Payment Failed – Reservation #" + reservationId;
        String body = """
                Hi Customer,
                We were unable to process your payment for the reservation.
                
                Reservation ID : %d
                Payment Status : FAILED
                Please try again to complete your booking.
                — Hotel Reservation System
                """.formatted(reservationId);

        log.info("Generated payment failed email for reservationId: {}", reservationId);

        return new EmailDto(null, subject, body);
    }

    public EmailDto reservationCancelled(Long reservationId) {
        String subject = "Reservation Cancelled – Booking #" + reservationId;
        String body = """
                Hi Customer,
                Your reservation has been successfully cancelled.
                
                Reservation ID : %d
                Status         : CANCELLED
                If this was not intended, please create a new reservation.
                — Hotel Reservation System
                """.formatted(reservationId);

        log.info("Generated reservation cancelled email for reservationId: {}", reservationId);

        return new EmailDto(null, subject, body);
    }


    public EmailDto notificationRetryFailed(Long notificationId, int retryCount) {
        String subject = "Notification Delivery Failed – Notification #" + notificationId;
        String body = """
                Hi Admin,
                A notification has failed to be delivered even after multiple retry attempts.
                
                Notification ID : %d
                Status          : FAILED
                Retry Count     : %d
                Manual intervention may be required.
                — Hotel Reservation System
                """.formatted(notificationId, retryCount);

        log.warn("Generated retry failure email for notificationId: {}", notificationId);

        return new EmailDto(ADMIN_EMAIL, subject, body);
    }

    public EmailDto notificationRetrySuccess(Long notificationId) {
        String subject = "Notification Successfully Delivered After Retry";
        String body = """
                Hi Admin,
                A previously failed notification has been successfully delivered after retry.
                
                Notification ID : %d
                Final Status    : SENT
                — Hotel Reservation System
                """.formatted(notificationId);

        log.info("Generated retry success email for notificationId: {}", notificationId);

        return new EmailDto(ADMIN_EMAIL, subject, body);
    }
}