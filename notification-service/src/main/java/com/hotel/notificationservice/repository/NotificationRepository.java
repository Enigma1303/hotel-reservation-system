package com.hotel.notificationservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.notificationservice.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>   
{
List<Notification>findByStatus(Notification.NotificationStatus status);
}
