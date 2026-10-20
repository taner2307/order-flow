package com.orderflow.orderservice.controller;

import com.orderflow.orderservice.dto.CreateOrderRequest;
import com.orderflow.orderservice.dto.OrderResponse;
import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {

       Order order = orderService.createOrder(request);
       return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }


}
