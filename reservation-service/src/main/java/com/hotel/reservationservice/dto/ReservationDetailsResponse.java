package com.hotel.reservationservice.dto;

import com.hotel.reservationservice.entity.Reservation;
import java.time.LocalDateTime;

public class ReservationDetailsResponse {
    private Long reservationId;
    private Reservation.ReservationStatus status;
    private String customerName;
    private Long roomId;
    private String roomType;

    private String paymentStatus;
    private LocalDateTime createdAt;

    public ReservationDetailsResponse(Long reservationId,
                                       Reservation.ReservationStatus status,
                                       String customerName,
                                        Long roomId,
                                       String roomType,
                                       String paymentStatus,
                                       LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.status = status;
        this.customerName = customerName;
        this.roomId = roomId;
        this.roomType = roomType;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Reservation.ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(Reservation.ReservationStatus status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

 
}