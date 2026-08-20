package com.zephyr.FoodApp.order.services;

import com.zephyr.FoodApp.enums.OrderStatus;
import com.zephyr.FoodApp.order.dtos.OrderDTO;
import com.zephyr.FoodApp.order.dtos.OrderItemDTO;
import com.zephyr.FoodApp.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?> placeOrderFromCart();
    Response<OrderDTO> getOrderById(Long id);
    Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDTO>> getOrdersOfUser();
    Response<OrderItemDTO> getOrderItemById(Long orderItemId);
    Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO);
    Response<Long> countUniqueCustomers();
}
