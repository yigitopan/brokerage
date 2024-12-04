package com.opan.brokerageapi.services;

import com.opan.brokerageapi.entities.Asset;
import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.entities.Order;
import com.opan.brokerageapi.entities.Order.OrderSide;
import com.opan.brokerageapi.entities.Order.OrderStatus;
import com.opan.brokerageapi.repositories.AssetRepository;
import com.opan.brokerageapi.repositories.OrderRepository;
import com.opan.brokerageapi.repositories.CustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Order createOrder(Long customerId, String assetName, OrderSide side, Integer size, BigDecimal price) {
        Customer customer = customerService.getCustomerById(customerId);

        // Validate and update balances
        if (side == OrderSide.BUY) {
            BigDecimal totalCost = price.multiply(new BigDecimal(size));
            if (customer.getBalanceTRY() < totalCost.intValue()) {
                throw new IllegalArgumentException("Insufficient TRY balance for BUY order");
            }
            customer.setBalanceTRY(customer.getBalanceTRY() - totalCost.intValue());
        } else if (side == OrderSide.SELL) {
            // Ensure the customer has enough usable asset size
            Asset asset = assetRepository.findByCustomerAndAssetNameContainingIgnoreCase(customer, assetName)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Asset not found for customer"));

            if (asset.getUsableSize() < size) {
                throw new IllegalArgumentException("Insufficient asset size for SELL order");
            }
            asset.setUsableSize(asset.getUsableSize() - size);
            assetRepository.save(asset);
        }

        // Create and save the order
        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setOrderSide(side);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Attempt to match orders
        matchOrders(savedOrder);

        return savedOrder;
    }

    @Transactional
    public void matchOrders(Order newOrder) {
        if (newOrder.getStatus() != OrderStatus.PENDING) {
            return;
        }

        if (newOrder.getOrderSide() == OrderSide.BUY) {
            // Find SELL orders with price <= newOrder.price
            List<Order> matchingSellOrders = orderRepository.findByAssetNameAndOrderSideAndPriceLessThanEqual(
                    newOrder.getAssetName(),
                    OrderSide.SELL,
                    newOrder.getPrice()
            );

            for (Order sellOrder : matchingSellOrders) {
                if (sellOrder.getStatus() != OrderStatus.PENDING) {
                    continue;
                }

                int matchSize = Math.min(newOrder.getSize(), sellOrder.getSize());

                newOrder.setSize(newOrder.getSize() - matchSize);
                sellOrder.setSize(sellOrder.getSize() - matchSize);

                if (sellOrder.getSize() == 0) {
                    sellOrder.setStatus(OrderStatus.MATCHED);
                    // Update Seller's balance
                    Customer seller = sellOrder.getCustomer();
                    BigDecimal totalPrice = sellOrder.getPrice().multiply(new BigDecimal(matchSize));
                    seller.setBalanceTRY(seller.getBalanceTRY() + totalPrice.intValue());
                    customerRepository.save(seller);
                }

                if (newOrder.getSize() == 0) {
                    newOrder.setStatus(OrderStatus.MATCHED);
                }

                orderRepository.save(sellOrder);
                orderRepository.save(newOrder);

                // Update Buyer's asset
                Asset buyerAsset = assetRepository.findByCustomerAndAssetNameContainingIgnoreCase(
                                newOrder.getCustomer(), newOrder.getAssetName())
                        .stream()
                        .findFirst()
                        .orElse(null);

                if (buyerAsset == null) {
                    buyerAsset = new Asset();
                    buyerAsset.setCustomer(newOrder.getCustomer());
                    buyerAsset.setAssetName(newOrder.getAssetName());
                    buyerAsset.setSize(matchSize);
                    buyerAsset.setUsableSize(matchSize);
                } else {
                    buyerAsset.setSize(buyerAsset.getSize() + matchSize);
                    buyerAsset.setUsableSize(buyerAsset.getUsableSize() + matchSize);
                }
                assetRepository.save(buyerAsset);

                if (newOrder.getStatus() == OrderStatus.MATCHED) {
                    break;
                }
            }

        } else if (newOrder.getOrderSide() == OrderSide.SELL) {
            // Find BUY orders with price >= newOrder.price
            List<Order> matchingBuyOrders = orderRepository.findByAssetNameAndOrderSideAndPriceGreaterThanEqual(
                    newOrder.getAssetName(),
                    OrderSide.BUY,
                    newOrder.getPrice()
            );

            for (Order buyOrder : matchingBuyOrders) {
                if (buyOrder.getStatus() != OrderStatus.PENDING) {
                    continue;
                }

                // Determine match size
                int matchSize = Math.min(newOrder.getSize(), buyOrder.getSize());

                // Update order statuses
                newOrder.setSize(newOrder.getSize() - matchSize);
                buyOrder.setSize(buyOrder.getSize() - matchSize);

                if (buyOrder.getSize() == 0) {
                    buyOrder.setStatus(OrderStatus.MATCHED);
                    // Update Buyers asset
                    Asset buyerAsset = assetRepository.findByCustomerAndAssetNameContainingIgnoreCase(
                                    buyOrder.getCustomer(), buyOrder.getAssetName())
                            .stream()
                            .findFirst()
                            .orElse(null);

                    if (buyerAsset == null) {
                        buyerAsset = new Asset();
                        buyerAsset.setCustomer(buyOrder.getCustomer());
                        buyerAsset.setAssetName(buyOrder.getAssetName());
                        buyerAsset.setSize(matchSize);
                        buyerAsset.setUsableSize(matchSize);
                    } else {
                        buyerAsset.setSize(buyerAsset.getSize() + matchSize);
                        buyerAsset.setUsableSize(buyerAsset.getUsableSize() + matchSize);
                    }
                    assetRepository.save(buyerAsset);
                }

                if (newOrder.getSize() == 0) {
                    newOrder.setStatus(OrderStatus.MATCHED);
                }

                orderRepository.save(buyOrder);
                orderRepository.save(newOrder);

                // Update Sellers balance
                Customer seller = newOrder.getCustomer();
                BigDecimal totalPrice = buyOrder.getPrice().multiply(new BigDecimal(matchSize));
                seller.setBalanceTRY(seller.getBalanceTRY() + totalPrice.intValue());
                customerRepository.save(seller);

                if (newOrder.getStatus() == OrderStatus.MATCHED) {
                    break;
                }
            }
        }
    }
    public List<Order> listOrders(Long customerId,
                                  LocalDateTime startDate,
                                  LocalDateTime endDate,
                                  OrderStatus status,
                                  String assetName) {
        Customer customer = customerService.getCustomerById(customerId);
        return orderRepository.findByCustomerAndCreateDateBetween(customer, startDate, endDate);
    }

    @Transactional
    public void cancelOrder(Long customerId, Long orderId) {
        Customer customer = customerService.getCustomerById(customerId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be canceled");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal totalCost = order.getPrice().multiply(new BigDecimal(order.getSize()));
            customer.setBalanceTRY(customer.getBalanceTRY() + totalCost.intValue());
            customerRepository.save(customer);
        } else if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = assetRepository.findByCustomerAndAssetNameContainingIgnoreCase(
                            customer, order.getAssetName())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Asset not found for customer"));

            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            assetRepository.save(asset);
        }
    }

}