package com.hotel.reservationservice.dto;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import com.hotel.reservationservice.entity.Reservation;
import com.hotel.reservationservice.entity.Reservation.ReservationStatus;

public class ReservationResponse
{
private long id;
private long customerId;
private long roomId;    
private Reservation.ReservationStatus status;
private LocalDateTime createdAt;
public ReservationResponse(long id, long customerId, long roomId, ReservationStatus status, LocalDateTime createdAt) {
    this.id = id;
    this.customerId = customerId;
    this.roomId = roomId;
    this.status = status;
    this.createdAt = createdAt;
}
public long getId() {
    return id;
}
public void setId(long id) {
    this.id = id;
}
public long getCustomerId() {
    return customerId;
}
public void setCustomerId(long customerId) {
    this.customerId = customerId;
}
public long getRoomId() {
    return roomId;
}
public void setRoomId(long roomId) {
    this.roomId = roomId;
}
public Reservation.ReservationStatus getStatus() {
    return status;
}
public void setStatus(Reservation.ReservationStatus status) {
    this.status = status;
}
public LocalDateTime getCreatedAt() {
    return createdAt;
}
public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
}


}
