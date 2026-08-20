package com.zephyr.FoodApp.auth_users.services;

import com.zephyr.FoodApp.auth_users.dtos.UserDTO;
import com.zephyr.FoodApp.auth_users.entity.User;
import com.zephyr.FoodApp.auth_users.repository.UserRepository;
import com.zephyr.FoodApp.email_notification.dtos.NotificationDTO;
import com.zephyr.FoodApp.email_notification.services.NotificationService;
import com.zephyr.FoodApp.exceptions.BadRequestException;
import com.zephyr.FoodApp.exceptions.NotFoundException;
import com.zephyr.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {
        log.info("INSIDE getAllUsers()");

        List<User> userList = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOS = modelMapper.map(userList, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All users retrieved successfully")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {

        log.info("INSIDE getOwnAccountDetails()");
        User user = getCurrentLoggedInUser();

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {

        log.info("INSIDE updateOwnAccount()");

        // fetch current logged in user
        User user = getCurrentLoggedInUser();

        String profileUrl = user.getProfileUrl();

        MultipartFile imageFile = userDTO.getImageFile();

        // check if new imageFile was provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String newProfileUrl = saveImageLocally(imageFile, profileUrl);
            user.setProfileUrl(newProfileUrl);
        } else if (userDTO.getProfileUrl() != null) {
            user.setProfileUrl(userDTO.getProfileUrl());
        }

        // update user details
        if (userDTO.getName() != null) user.setName(userDTO.getName());
        if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());
        if (userDTO.getPassword() != null) user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())){
            if (userRepository.existsByEmail(userDTO.getEmail())){
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }

        // save the user
        userRepository.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account updated successfully")
                .build();
    }

    private String saveImageLocally(MultipartFile file, String oldProfileUrl) {
        if (oldProfileUrl != null && !oldProfileUrl.isEmpty()) {
            try {
                String cleanPath = oldProfileUrl.startsWith("/") ? oldProfileUrl.substring(1) : oldProfileUrl;
                Path oldPath = Paths.get(cleanPath);
                Files.deleteIfExists(oldPath);
                log.info("Deleted old profile image: {}", oldProfileUrl);
            } catch (Exception e) {
                log.warn("Could not delete old profile image: {}", oldProfileUrl, e);
            }
        }

        try {
            String uploadDir = "uploads/profile/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String imageName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + imageName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + imageName;
        } catch (IOException e) {
            log.error("Failed to save image locally", e);
            throw new BadRequestException("Failed to save profile image");
        }
    }

    @Override
    public Response<?> deactiveOwnAccount() {

        log.info("INSIDE deactiveOwnAccount()");

        User user = getCurrentLoggedInUser();

        // Deactive the user
        user.setActive(false);
        userRepository.save(user);

        // SEND EMAIL AFTER DEACTIVATION

        // Send email notification
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Account Deactivated")
                .body("Your account has been deactivated. If this was a mistake, please contact support.")
                .build();
        notificationService.sendEmail(notificationDTO);

        // Return a success response
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account deactivated successfully")
                .build();
    }
}
