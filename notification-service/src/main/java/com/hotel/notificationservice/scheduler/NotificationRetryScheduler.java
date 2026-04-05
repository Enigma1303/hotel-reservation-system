package com.hotel.notificationservice.scheduler;

import com.hotel.notificationservice.dto.EmailDto;
import com.hotel.notificationservice.entity.Notification;
import com.hotel.notificationservice.repository.NotificationRepository;
import com.hotel.notificationservice.service.EmailSenderService;
import com.hotel.notificationservice.service.EmailTemplateService;
import com.hotel.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class NotificationRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmailSenderService emailSenderService;
    private final EmailTemplateService emailTemplateService;

    @Value("${notification.retry.max-attempts}")
    private int maxRetryAttempts;

    public NotificationRetryScheduler(NotificationRepository notificationRepository,
                                       NotificationService notificationService,
                                       EmailSenderService emailSenderService,
                                       EmailTemplateService emailTemplateService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.emailSenderService = emailSenderService;
        this.emailTemplateService = emailTemplateService;
    }

    @Scheduled(fixedDelayString = "${notification.retry.interval}")
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository
            .findByStatus(Notification.NotificationStatus.FAILED);

        if (failedNotifications.isEmpty()) {
            return;
        }

        log.info("Found {} failed notifications to retry", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            if (notification.getRetryCount() >= maxRetryAttempts) {
                log.warn("Notification {} has reached max retry attempts, skipping",
                    notification.getId());
                continue;
            }

            log.info("Auto retrying notification id: {} attempt: {}",
                notification.getId(), notification.getRetryCount() + 1);

            notification.setRetryCount(notification.getRetryCount() + 1);

            try {
                EmailDto retryEmail = new EmailDto(
                    notification.getRecipientEmail(),
                    notification.getSubject() ,
                    notification.getMessage());
                emailSenderService.sendEmail(retryEmail);
                notification.setStatus(Notification.NotificationStatus.SENT);
                log.info("Notification {} successfully sent on retry attempt {}",
                    notification.getId(), notification.getRetryCount());
            } catch (Exception e) {
                notification.setStatus(Notification.NotificationStatus.FAILED);
                log.error("Notification {} failed on retry attempt {} error: {}",
                    notification.getId(), notification.getRetryCount(), e.getMessage());

                if (notification.getRetryCount() >= maxRetryAttempts) {
                    log.warn("Notification {} exhausted all retries, admin intervention required",
                        notification.getId());
                    EmailDto adminEmail = emailTemplateService.notificationRetryFailed(
                        notification.getId(), notification.getRetryCount());
                    try {
                        emailSenderService.sendEmail(adminEmail);
                    } catch (Exception adminMailEx) {
                        log.error("Failed to send admin alert email: {}", adminMailEx.getMessage());
                    }
                }
            }

            notificationRepository.save(notification);
        }
    }
}