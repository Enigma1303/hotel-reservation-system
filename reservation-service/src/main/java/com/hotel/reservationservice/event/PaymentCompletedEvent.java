package com.hotel.reservationservice.event;

import java.time.LocalDateTime;

public class PaymentCompletedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private Payload payload;

    public PaymentCompletedEvent() {}

    public PaymentCompletedEvent(String eventId, String eventType,
                                  LocalDateTime timestamp, Payload payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public static class Payload {
        private Long reservationId;
        private Long paymentId;
        private String status;
         private String customerEmail;

        public Payload() {}

        public Payload(Long reservationId, Long paymentId, String status, String customerEmail) {
            this.reservationId = reservationId;
            this.paymentId = paymentId;
            this.status = status;
            this.customerEmail = customerEmail;
        }

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }

        public Long getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(Long paymentId) {
            this.paymentId = paymentId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
        public String getCustomerEmail() {
            return customerEmail;
        }
        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }


}