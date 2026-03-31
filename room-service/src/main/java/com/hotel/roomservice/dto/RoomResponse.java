package com.hotel.roomservice.dto;

import com.hotel.roomservice.entity.Room;

public class RoomResponse {
    private Long id;
    private Room.RoomType type;
    private Boolean availability;
    private Double price;

    public RoomResponse(Long id, Room.RoomType type, Boolean availability, Double price) {
        this.id = id;
        this.type = type;
        this.availability = availability;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Room.RoomType getType() {
        return type;
    }

    public void setType(Room.RoomType type) {
        this.type = type;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


}