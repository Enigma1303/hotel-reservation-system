package com.hotel.reservationservice.event;

import java.time.LocalDateTime;

public class ReservationCreatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private Payload payload;

    public ReservationCreatedEvent() {}

    public ReservationCreatedEvent(String eventId, String eventType,
                                    LocalDateTime timestamp, Payload payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public static class Payload {
        private Long reservationId;
        private Long customerId;
        private Long roomId;
        private String customerEmail;

        public Payload() {}

        public Payload(Long reservationId, Long customerId, Long roomId,String customerEmail) {
            this.reservationId = reservationId;
            this.customerId = customerId;
            this.roomId = roomId;
            this.customerEmail = customerEmail;
        }

        public Long getReservationId()
        {
            return reservationId;
        }
        public void setReservationId(Long reservationId)
        {
            this.reservationId = reservationId;
        }
        public Long getCustomerId()
        { 
            return customerId;

        }
        public void setCustomerId(Long customerId)
        { 
            this.customerId = customerId;
        }
        public Long getRoomId()
        { 
            return roomId; 
        }
        public void setRoomId(Long roomId) 
        { 
            this.roomId = roomId;
        }
        public String getCustomerEmail() {
            return customerEmail;
        }
        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
    }
    }
    public String getEventId()
    {
        return eventId;
    }
    public void setEventId(String eventId)
    {
        this.eventId = eventId;
    }
    public String getEventType()
    {
        return eventType;
    }
    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }
    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
    }
    public Payload getPayload()
    { 
        return payload;
    }
    public void setPayload(Payload payload) 
    { 
        this.payload = payload;
    }
   
}