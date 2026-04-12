package com.hotel.roomservice.repository;

import com.hotel.roomservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByAvailability(Boolean availability);


    @Modifying
@Transactional
@Query("UPDATE Room r SET r.availability = false WHERE r.id = :roomId AND r.availability = true")
int bookRoom(@Param("roomId") Long roomId);
}

