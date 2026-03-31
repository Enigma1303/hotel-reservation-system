package com.hotel.roomservice.service;

import com.hotel.roomservice.dto.RoomAvailabilityRequest;
import com.hotel.roomservice.dto.RoomResponse;
import com.hotel.roomservice.entity.Room;
import com.hotel.roomservice.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(room -> new RoomResponse(
                        room.getId(),
                        room.getType(),
                        room.getAvailability(),
                        room.getPrice()))
                .collect(Collectors.toList());
    }

    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        return new RoomResponse(room.getId(), room.getType(), room.getAvailability(), room.getPrice());
    }

    public RoomResponse updateAvailability(Long id, RoomAvailabilityRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        if (request.getAvailability() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability cannot be null");
        }
        room.setAvailability(request.getAvailability());
        Room updated = roomRepository.save(room);
        return new RoomResponse(updated.getId(), updated.getType(), updated.getAvailability(), updated.getPrice());
    }
}