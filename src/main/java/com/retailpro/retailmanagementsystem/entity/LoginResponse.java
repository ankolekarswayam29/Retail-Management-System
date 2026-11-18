package com.retailpro.retailmanagementsystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;


    @Data
    @AllArgsConstructor
    public class LoginResponse {

        private String message;
        private Long userId;
        private String username;
        private String email;
        private Role roleId;
    }
