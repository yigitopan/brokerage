package com.opan.brokerageapi.services;

import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.repositories.CustomerRepository;
import com.opan.brokerageapi.security.JwtUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private CustomerRepository customerRepository;

    public UserDetailsServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email);
        return JwtUserDetails.create(customer);
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));
        return JwtUserDetails.create(customer);
    }
}