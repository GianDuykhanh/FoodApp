package com.zephyr.FoodApp.payment.services;

import com.zephyr.FoodApp.payment.dtos.PaymentDTO;
import com.zephyr.FoodApp.response.Response;

import java.util.List;

public interface PaymentService {

    Response<?> initializePayment(PaymentDTO paymentDTO);
    void updatePaymentForOrder(PaymentDTO paymentDTO);
    Response<List<PaymentDTO>> getAllPayments();
    Response<PaymentDTO> getPaymentById(Long paymentId);

}
