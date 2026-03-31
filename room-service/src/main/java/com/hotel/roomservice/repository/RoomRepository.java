package com.hotel.roomservice.repository;

import com.hotel.roomservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByAvailability(Boolean availability);
}