package com.hotel.roomservice.controller;

import com.hotel.roomservice.dto.RoomAvailabilityRequest;
import com.hotel.roomservice.dto.RoomResponse;
import com.hotel.roomservice.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RoomResponse> updateAvailability(
            @PathVariable Long id,
            @RequestBody RoomAvailabilityRequest request) {
        return ResponseEntity.ok(roomService.updateAvailability(id, request));
    }
}