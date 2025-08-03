package com.ak.trailerji.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// JwtResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private Long id;

    public JwtResponse(String token, String username, String email, Long id) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.id = id;
    }
}
