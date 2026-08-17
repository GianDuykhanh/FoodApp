package com.zephyr.FoodApp;

import com.zephyr.FoodApp.email_notification.dtos.NotificationDTO;
import com.zephyr.FoodApp.email_notification.services.NotificationService;
import com.zephyr.FoodApp.enums.NotificationType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FoodAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodAppApplication.class, args);
	}

//	@Bean
//	CommandLineRunner runner(NotificationService notificationService) {
//		return args -> {
//			NotificationDTO notificationDTO = NotificationDTO.builder()
//					.recipient("duykhanh882021@gmail.com")
//					.subject("Hello Duy Khanh")
//					.body("Hey this is a test email")
//					.type(NotificationType.EMAIL)
//					.build();
//
//			notificationService.sendEmail(notificationDTO);
//		};
//	}

}
