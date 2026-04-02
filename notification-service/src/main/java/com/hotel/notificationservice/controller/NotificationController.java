package com.hotel.notificationservice.controller;

import com.hotel.notificationservice.entity.Notification;
import com.hotel.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        log.info("GET /api/notifications called");
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Notification> retryNotification(@PathVariable Long id) {
        log.info("POST /api/notifications/{}/retry called", id);
        return ResponseEntity.ok(notificationService.retryNotification(id));
    }
}