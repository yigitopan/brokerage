package com.opan.brokerageapi.requests;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String email;
    private String password;
    private UserType type;

    public enum UserType {
        ADMIN,
        CUSTOMER
    }
}