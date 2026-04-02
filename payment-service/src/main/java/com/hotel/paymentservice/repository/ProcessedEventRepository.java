package com.hotel.paymentservice.repository;

import com.hotel.paymentservice.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

//for checking if the event has already been processed to ensure idempotency
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventId(String eventId);
}