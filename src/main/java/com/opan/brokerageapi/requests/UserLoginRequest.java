package com.opan.brokerageapi.requests;


import lombok.Data;

@Data
public class UserLoginRequest {
    String email;
    String password;

}