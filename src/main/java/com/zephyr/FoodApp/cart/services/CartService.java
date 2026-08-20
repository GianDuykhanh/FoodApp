package com.zephyr.FoodApp.cart.services;


import com.zephyr.FoodApp.cart.dtos.CartDTO;
import com.zephyr.FoodApp.response.Response;

public interface CartService {

    Response<?> addItemToCart(CartDTO cartDTO);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeItem(Long cartItemId);
    Response<CartDTO> getShoppingCart();
    Response<?> clearShoppingCart();
}
