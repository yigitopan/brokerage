package com.opan.brokerageapi.repositories;

import com.opan.brokerageapi.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository <Customer, Long> {

    Customer findByEmail(String username);

}

