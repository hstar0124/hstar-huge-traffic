package com.hstar.backend.dto;

import lombok.Getter;

@Getter
public class SignUpUserRequest {
    String username;
    String password;
    String email;
}