package com.opan.brokerageapi.dtos;

import com.opan.brokerageapi.entities.Asset;
import com.opan.brokerageapi.entities.Order;
import lombok.Data;
import java.util.Set;

@Data
public class CustomerDto {
    private Long id;
    private String name;
    private String email;
    private Integer balanceTRY;
    private Set<Asset> assets;
    private Set<Order> orders;
    private Set<String> roles;

    public CustomerDto(Long id, String name, String email, Integer balanceTRY, Set<Asset> assets, Set<Order> orders, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.balanceTRY = balanceTRY;
        this.assets = assets;
        this.orders = orders;
        this.roles = roles;
    }
}