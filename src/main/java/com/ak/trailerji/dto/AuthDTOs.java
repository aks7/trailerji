package com.ak.trailerji.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// UserDto.java
@Data
@NoArgsConstructor
@AllArgsConstructor
class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
}