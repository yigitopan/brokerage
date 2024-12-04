package com.opan.brokerageapi.controllers;

import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.services.CustomerService;
import com.opan.brokerageapi.requests.DepositRequestDto;
import com.opan.brokerageapi.requests.WithdrawRequestDto;
import com.opan.brokerageapi.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{customerId}")
    @PreAuthorize("(hasRole('CUSTOMER') and #customerId == principal.id) or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Customer>> getCustomer(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer retrieved successfully", customer));
    }


    @PostMapping("/{customerId}/deposit")
    @PreAuthorize("(hasRole('CUSTOMER') and #customerId == principal.id) or hasRole('ADMIN')")
    public ResponseEntity< ApiResponse <String> > depositMoney(
            @PathVariable Long customerId,
            @RequestBody DepositRequestDto depositRequest) {
        Customer updatedCustomer = customerService.depositMoney(customerId, depositRequest.getAmountTRY());
        return ResponseEntity.ok(new ApiResponse<>(true, "Deposit successful. New balance: " + updatedCustomer.getBalanceTRY()));
    }

    @PostMapping("/{customerId}/withdraw")
    @PreAuthorize("(hasRole('CUSTOMER') and #customerId == principal.id) or hasRole('ADMIN')")
    public ResponseEntity < ApiResponse <String> > withdrawMoney(
            @PathVariable Long customerId,
            @RequestBody WithdrawRequestDto withdrawRequest) {
        Customer updatedCustomer = customerService.withdrawMoney(customerId, withdrawRequest.getAmountTRY(), withdrawRequest.getIban());
        return ResponseEntity.ok(new ApiResponse<>(true, "Withdraw successful. New balance: " + updatedCustomer.getBalanceTRY()));
    }

}