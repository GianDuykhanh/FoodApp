package com.zephyr.FoodApp.auth_users.services;

import com.zephyr.FoodApp.auth_users.dtos.UserDTO;
import com.zephyr.FoodApp.auth_users.entity.User;
import com.zephyr.FoodApp.response.Response;

import java.util.List;

public interface UserService {

    User getCurrentLoggedInUser();

    Response<List<UserDTO>> getAllUsers();

    Response<UserDTO> getOwnAccountDetails();

    Response<?> updateOwnAccount(UserDTO userDTO);

    Response<?> deactiveOwnAccount();
}
