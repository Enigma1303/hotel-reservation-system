package com.hotel.notificationservice.service;

import com.hotel.notificationservice.dto.EmailDto;
import com.hotel.notificationservice.entity.Notification;
import com.hotel.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailSenderService emailSenderService;

    @Value("${notification.retry.max-attempts}")
    private int maxRetryAttempts;

    public NotificationService(NotificationRepository notificationRepository,
                                EmailSenderService emailSenderService) {
        this.notificationRepository = notificationRepository;
        this.emailSenderService = emailSenderService;
    }

    public Notification createNotification(String message,String subject,String recipientEmail,
                                            Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setSubject(subject);
        notification.setRecipientEmail(recipientEmail);
        notification.setType(type);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with id: {}", saved.getId());
        return saved;
    }
    @Async
    public void sendNotification(Notification notification, EmailDto emailDto) {
        try {
            log.info("Sending {} notification id: {}",
                notification.getType(), notification.getId());
            emailSenderService.sendEmail(emailDto);
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

        

        notification.setRetryCount(notification.getRetryCount() + 1);

     EmailDto retryEmail = new EmailDto(
    notification.getRecipientEmail(),
    notification.getSubject(),
    notification.getMessage()
);
        sendNotification(notification, retryEmail);

        if (notification.getStatus() == Notification.NotificationStatus.SENT) {
            log.info("Notification {} successfully sent after retry", id);
        } else {
            log.warn("Notification {} failed after retry attempt {}",
                id, notification.getRetryCount());
        }

        return notification;
    }
}