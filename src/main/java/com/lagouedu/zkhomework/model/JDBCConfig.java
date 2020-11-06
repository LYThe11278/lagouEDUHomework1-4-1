package com.lagouedu.zkhomework.model;

import lombok.Data;

@Data
public class JDBCConfig {
    private String url;

    private String driver;

    private String username;

    private String password;
}
