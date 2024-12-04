package com.opan.brokerageapi.requests;

import lombok.Data;

@Data
public class WithdrawRequestDto {

    private Integer amountTRY;

    private String iban;
}