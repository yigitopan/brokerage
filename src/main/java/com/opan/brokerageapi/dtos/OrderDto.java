package com.opan.brokerageapi.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDto {
    private Long id;
    private String assetName;
    private String orderSide;
    private Integer size;
    private BigDecimal price;
    private String status;
    private LocalDateTime createDate;
}