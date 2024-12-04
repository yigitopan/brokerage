package com.opan.brokerageapi.controllers;

import com.opan.brokerageapi.entities.Order;
import com.opan.brokerageapi.entities.Order.OrderSide;
import com.opan.brokerageapi.exceptions.InvalidOrderSideException;
import com.opan.brokerageapi.exceptions.InvalidOrderStatusException;
import com.opan.brokerageapi.services.OrderService;
import com.opan.brokerageapi.requests.OrderRequestDto;
import com.opan.brokerageapi.entities.Order.OrderStatus;

import com.opan.brokerageapi.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @PostMapping
    @PreAuthorize("#customerId == principal.id")
    public ResponseEntity < ApiResponse < Order >> createOrder(
            @PathVariable Long customerId,
            @RequestBody OrderRequestDto orderRequest) {
        OrderSide side;
        try {
            side = OrderSide.valueOf(orderRequest.getOrderSide().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderSideException("Invalid order side.");
        }

        Order order = orderService.createOrder(
                customerId,
                orderRequest.getAssetName(),
                side,
                orderRequest.getSize(),
                orderRequest.getPrice()
        );

        ApiResponse< Order > response = new ApiResponse < > (true, "Order created successfully", order);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("(hasRole('CUSTOMER') and #customerId == principal.id) or hasRole('ADMIN')")
    public ResponseEntity< ApiResponse < List < Order >>> listOrders(
            @PathVariable Long customerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assetName) {

        LocalDateTime start = (startDate != null) ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusYears(200);
        LocalDateTime end = (endDate != null) ? LocalDateTime.parse(endDate) : LocalDateTime.now();

        OrderStatus orderStatus = null;
        if (status != null) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidOrderStatusException("Invalid order status.");
            }
        }

        List<Order> orders = orderService.listOrders(customerId, start, end, orderStatus, assetName);
        return ResponseEntity.ok(new ApiResponse < > (true, "Orders retrieved successfully", orders));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("(hasRole('CUSTOMER') and #customerId == principal.id) or hasRole('ADMIN')")
    public ResponseEntity< ApiResponse < String >> cancelOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId) {
        orderService.cancelOrder(customerId, orderId);
        return ResponseEntity.ok(new ApiResponse < > (true, "Order cancelled successfully", null));
    }

}