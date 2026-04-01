package com.hotel.reservationservice.dto;

public class ReservationRequest {
private long customerId;
private long roomId;
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


}
