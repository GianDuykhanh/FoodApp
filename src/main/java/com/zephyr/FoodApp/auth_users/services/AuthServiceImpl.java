package com.zephyr.FoodApp.auth_users.services;

import com.zephyr.FoodApp.auth_users.dtos.LoginRequest;
import com.zephyr.FoodApp.auth_users.dtos.LoginResponse;
import com.zephyr.FoodApp.auth_users.dtos.RegistrationRequest;
import com.zephyr.FoodApp.auth_users.entity.User;
import com.zephyr.FoodApp.auth_users.repository.UserRepository;
import com.zephyr.FoodApp.exceptions.BadRequestException;
import com.zephyr.FoodApp.exceptions.NotFoundException;
import com.zephyr.FoodApp.response.Response;
import com.zephyr.FoodApp.role.entity.Role;
import com.zephyr.FoodApp.role.repository.RoleRepository;
import com.zephyr.FoodApp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;

    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        log.info("INSIDE register()");

        if (userRepository.existsByEmail(registrationRequest.getEmail())){
            throw new BadRequestException("Email Already exists");
        }

        // Collect all roles from the request
        List<Role> userRoles;

        if(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()){
            userRoles = registrationRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(() -> new NotFoundException("Role with name: " + roleName + "Not found")))
                    .toList();
        }
        else {
            // if no roles provided, default to customer
            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("Default CUSTOMER Role not found"));
            userRoles = List.of(defaultRole);
        }

        // build the user object
        User userToSave = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .address(registrationRequest.getAddress())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // save the user
        userRepository.save(userToSave);

        log.info("user registered successfully");

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User Registered Successfully")
                .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {

        log.info("INSIDE login()");
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Invalid Email"));

        if (!user.isActive()){
            throw new NotFoundException("Account not active, Please contact customer support");
        }

        // verify the password
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new BadRequestException("Invalid Password");
        }

        // Generate a token

        String token = jwtUtils.generateToken(user.getEmail());

        // Extract roles names as a list
        List<String> roleName = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roleName);

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login Successful")
                .data(loginResponse)
                .build();
    }
}
