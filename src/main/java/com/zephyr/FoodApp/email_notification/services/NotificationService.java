package com.zephyr.FoodApp.email_notification.services;

import com.zephyr.FoodApp.email_notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
}
