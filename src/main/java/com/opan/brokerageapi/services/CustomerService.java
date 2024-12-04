package com.opan.brokerageapi.services;

import com.opan.brokerageapi.dtos.CustomerDto;
import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.exceptions.InsufficientBalanceException;
import com.opan.brokerageapi.exceptions.InvalidIBANException;
import com.opan.brokerageapi.repositories.CustomerRepository;
import com.opan.brokerageapi.utils.IBANValidator;
import jakarta.transaction.Transactional; // TODO: is this the correct import?
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    CustomerRepository customerRepository;
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllUsers() {
        return customerRepository.findAll();
    }

    public Customer saveOneUser(Customer newUser) {
        return customerRepository.save(newUser);
    }

    public Customer getOneUser(Long userId) {
        return customerRepository.findById(userId).orElse(null);
    }

    public Customer getOneUserByEmail(String username) {
        return customerRepository.findByEmail(username);
    }

    public Customer updateOneUser(Long userId, Customer updatedUser) {
        Optional<Customer> userOptional = customerRepository.findById(userId);
        if(userOptional.isPresent()) {
            Customer foundUser = userOptional.get();
            foundUser.setEmail(updatedUser.getEmail());
            foundUser.setPassword(updatedUser.getPassword());
            customerRepository.save(foundUser);
            return foundUser;
        }
        return null;
    }

    public void deleteById(Long userId) {
        customerRepository.deleteById(userId);
    }

    public Customer getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        return customer;
    }

    @Transactional
    public Customer depositMoney(Long customerId, Integer amountTRY) {
        if (amountTRY <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Customer customer = getCustomerById(customerId);
        customer.setBalanceTRY(customer.getBalanceTRY() + amountTRY);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer withdrawMoney(Long customerId, Integer amountTRY, String IBAN) {
        validateWithdrawalRequest(amountTRY, IBAN);

        Customer customer = getCustomerById(customerId);

        if (!hasSufficientBalance(customer, amountTRY)) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        customer.setBalanceTRY(customer.getBalanceTRY() - amountTRY);
        return customerRepository.save(customer);
    }

    private void validateWithdrawalRequest(Integer amountTRY, String IBAN) {
        if (amountTRY <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (!IBANValidator.isValid(IBAN)) {
            throw new InvalidIBANException("Invalid IBAN");
        }
    }

    private boolean hasSufficientBalance(Customer customer, Integer amountTRY) {
        return customer.getBalanceTRY() >= amountTRY;
    }

    public CustomerDto convertToDto(Customer updatedCustomer) {
        return new CustomerDto(
                updatedCustomer.getId(),
                updatedCustomer.getName(),
                updatedCustomer.getEmail(),
                updatedCustomer.getBalanceTRY(),
                updatedCustomer.getAssets(),
                updatedCustomer.getOrders(),
                updatedCustomer.getRoles()
        );
    }
}