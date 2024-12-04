package com.opan.brokerageapi.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequestDto {
    private String assetName;
    private String orderSide; // BUY or SELL
    private Integer size;
    private BigDecimal price;
}