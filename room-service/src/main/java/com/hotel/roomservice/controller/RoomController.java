package com.hotel.roomservice.controller;

import com.hotel.roomservice.dto.RoomAvailabilityRequest;
import com.hotel.roomservice.dto.RoomResponse;
import com.hotel.roomservice.service.RoomService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
     

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);
    private final RoomService roomService;
    private final Environment environment;
    
    public RoomController(RoomService roomService, Environment environment) {
        this.roomService = roomService;
        this.environment = environment;

    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {

        log.info("Request handled by instance on port: {}",
        environment.getProperty("local.server.port"));
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

    @PostMapping("/{id}/book")
public ResponseEntity<Map<String, Boolean>> bookRoom(@PathVariable Long id) {
    boolean success = roomService.bookRoom(id);
    if (!success) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is already booked");
    }
    return ResponseEntity.ok(Map.of("success", true));
}

  
}
