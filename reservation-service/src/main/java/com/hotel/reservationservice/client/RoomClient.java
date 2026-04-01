package com.hotel.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.hotel.reservationservice.dto.RoomDto;

import java.util.Map;

@FeignClient(name = "room-service")
public interface RoomClient {
    @GetMapping("/api/rooms/{id}")
    RoomDto getRoomById(@PathVariable("id") Long id);

    @PatchMapping("/api/rooms/{id}")
    RoomDto updateAvailability(@PathVariable("id") Long id,
                               @RequestBody Map<String, Object> request);
}