package com.opan.brokerageapi.dtos;

import lombok.Data;

@Data
public class AssetDto {
    private Long id;
    private String assetName;
    private Integer size;
    private Integer usableSize;
}