package com.hotel.reservationservice.dto;

public class RoomDto {
    private Long id;
    private String type;
    private Boolean availability;
    private Double price;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Boolean getAvailability() { return availability; }
    public void setAvailability(Boolean availability) { this.availability = availability; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}