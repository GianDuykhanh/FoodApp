package com.zephyr.FoodApp.auth_users.services;

import com.zephyr.FoodApp.auth_users.dtos.LoginRequest;
import com.zephyr.FoodApp.auth_users.dtos.LoginResponse;
import com.zephyr.FoodApp.auth_users.dtos.RegistrationRequest;
import com.zephyr.FoodApp.response.Response;

public interface AuthService {

    Response<?> register(RegistrationRequest registrationRequest);
    Response<LoginResponse> login(LoginRequest loginRequest);
}
