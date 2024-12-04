package com.opan.brokerageapi.repositories;

import com.opan.brokerageapi.entities.Order;
import com.opan.brokerageapi.entities.Order.OrderSide;
import com.opan.brokerageapi.entities.Order.OrderStatus;
import com.opan.brokerageapi.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerAndCreateDateBetween(Customer customer,
                                                   java.time.LocalDateTime start,
                                                   java.time.LocalDateTime end);

    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);

    List<Order> findByCustomerAndAssetNameContainingIgnoreCase(Customer customer, String assetName);

    List<Order> findByAssetNameAndOrderSideAndPriceLessThanEqual(String assetName, OrderSide orderSide,
                                                                 java.math.BigDecimal price);

    List<Order> findByAssetNameAndOrderSideAndPriceGreaterThanEqual(String assetName, OrderSide orderSide,
                                                                    java.math.BigDecimal price);

}