package com.opan.brokerageapi.requests;

import lombok.Data;

@Data
public class AssetRequestDto {
    private String assetName;
    private Integer size;
}
