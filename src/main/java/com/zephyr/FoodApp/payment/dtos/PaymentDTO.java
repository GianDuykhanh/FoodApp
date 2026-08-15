package com.zephyr.FoodApp.payment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zephyr.FoodApp.auth_users.dtos.UserDTO;
import com.zephyr.FoodApp.auth_users.entity.User;
import com.zephyr.FoodApp.enums.PaymentGateway;
import com.zephyr.FoodApp.enums.PaymentStatus;
import com.zephyr.FoodApp.order.dtos.OrderDTO;
import com.zephyr.FoodApp.order.entity.Order;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDTO {

    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private String transactionId;

    private PaymentGateway paymentGateway;

    private String failureReasons;

    private boolean success;

    private LocalDateTime paymentDate;

    private OrderDTO orders;

    private UserDTO user;
}
