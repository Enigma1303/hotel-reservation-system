package com.hotel.roomservice.service;

import com.hotel.roomservice.dto.RoomAvailabilityRequest;
import com.hotel.roomservice.dto.RoomResponse;
import com.hotel.roomservice.entity.Room;
import com.hotel.roomservice.exception.InvalidRoomAvailabilityException;
import com.hotel.roomservice.exception.RoomNotFoundException;
import com.hotel.roomservice.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomResponse> getAllRooms() {
        log.info("Fetching all rooms");

        List<RoomResponse> rooms = roomRepository.findAll()
                .stream()
                .map(room -> new RoomResponse(
                        room.getId(),
                        room.getType(),
                        room.getAvailability(),
                        room.getPrice()))
                .collect(Collectors.toList());

        log.info("Fetched {} rooms", rooms.size());
        return rooms;
    }

    public RoomResponse getRoomById(Long id) {
        log.info("Fetching room with id: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found with id: {}", id);
                    return new RoomNotFoundException(id);
                });

        log.info("Room found with id: {}", id);

        return new RoomResponse(
                room.getId(),
                room.getType(),
                room.getAvailability(),
                room.getPrice());
    }

    public RoomResponse updateAvailability(Long id, RoomAvailabilityRequest request) {
        log.info("Updating availability for room id: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found with id: {}", id);
                    return new RoomNotFoundException(id);
                });

        if (request.getAvailability() == null) {
            log.error("Availability is null for room id: {}", id);
            throw new InvalidRoomAvailabilityException();
        }

        room.setAvailability(request.getAvailability());
        Room updated = roomRepository.save(room);

        log.info("Room {} availability updated to {}", id, updated.getAvailability());

        return new RoomResponse(
                updated.getId(),
                updated.getType(),
                updated.getAvailability(),
                updated.getPrice());
    }

    @Transactional
    public boolean bookRoom(Long id) {
    
        log.info("Booking room with id: {}", id);
        int updated=roomRepository.bookRoom(id);
        return updated==1;
    }
            
}