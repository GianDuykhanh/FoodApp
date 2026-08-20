package com.zephyr.FoodApp.auth_users.services;

import com.zephyr.FoodApp.auth_users.dtos.UserDTO;
import com.zephyr.FoodApp.auth_users.entity.User;
import com.zephyr.FoodApp.auth_users.repository.UserRepository;
import com.zephyr.FoodApp.email_notification.dtos.NotificationDTO;
import com.zephyr.FoodApp.email_notification.entity.Notification;
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

import java.net.URL;
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
//    private final AWSS3Service awss3Service;

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
                .message("All users retreived successfully")
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
        if (imageFile != null && !imageFile.isEmpty()){
            // delete old image in cloud if it exists
            if (profileUrl != null && profileUrl.isEmpty()){
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/") + 1);
                awss3Service.deleteFile("profile/" + keyName);
                log.info("Deleted old profile image from s3");
            }

            // upload new image
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl = awss3Service.uploadFile("profile/" + imageName, imageFile);
            user.setProfileUrl(newImageUrl.toString());
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
        user.setEmail(userDTO.getEmail());

        // save the user
        userRepository.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account updated successfully")
                .build();

        return null;
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
                .body("Your account has been deactived. If this was mistake, please contact support.")
                .build();
        notificationService.sendEmail(notificationDTO);

        // Return a success response
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account deactivated successfully")
                .build();
    }
}
