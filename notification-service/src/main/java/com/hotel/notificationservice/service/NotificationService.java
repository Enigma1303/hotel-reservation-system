package com.hotel.notificationservice.service;

import com.hotel.notificationservice.entity.Notification;
import com.hotel.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailTemplateService emailTemplateService;

    @Value("${notification.retry.max-attempts}")
    private int maxRetryAttempts;

    public NotificationService(NotificationRepository notificationRepository,
                                EmailTemplateService emailTemplateService) {
        this.notificationRepository = notificationRepository;
        this.emailTemplateService = emailTemplateService;
    }

    public Notification createNotification(String message,
                                            Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with id: {}", saved.getId());
        return saved;
    }

    public void sendNotification(Notification notification) {
        try {
            log.info("Sending {} notification id: {}",
                notification.getType(), notification.getId());
            log.info("\n{}", notification.getMessage());
            notification.setStatus(Notification.NotificationStatus.SENT);
            log.info("Notification {} sent successfully", notification.getId());
        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            log.error("Failed to send notification id: {} error: {}",
                notification.getId(), e.getMessage());
        }
        notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationRepository.findAll();
    }

    public Notification retryNotification(Long id) {
        log.info("Retrying notification id: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Notification not found with id: {}", id);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found");
                });

        if (notification.getStatus() != Notification.NotificationStatus.FAILED) {
            log.warn("Notification {} is not in FAILED state, current state: {}",
                id, notification.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Notification is not in FAILED state");
        }

        if (notification.getRetryCount() >= maxRetryAttempts) {
            log.warn("Max retry attempts reached for notification id: {}", id);
            log.info("\n{}", emailTemplateService.notificationRetryFailed(
                id, notification.getRetryCount()));
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Max retry attempts reached");
        }

        notification.setRetryCount(notification.getRetryCount() + 1);
        sendNotification(notification);

        if (notification.getStatus() == Notification.NotificationStatus.SENT) {
            log.info("\n{}", emailTemplateService.notificationRetrySuccess(id));
        } else {
            log.warn("\n{}", emailTemplateService.notificationRetryFailed(
                id, notification.getRetryCount()));
        }

        return notification;
    }
}