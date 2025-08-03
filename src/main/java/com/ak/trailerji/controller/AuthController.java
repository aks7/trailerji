package com.ak.trailerji.controller;

import com.ak.trailerji.dto.*;
import com.ak.trailerji.entity.Role;
import com.ak.trailerji.entity.User;
import com.ak.trailerji.repository.RoleRepository;
import com.ak.trailerji.repository.UserRepository;
import com.ak.trailerji.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);
            
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            return ResponseEntity.ok(new JwtResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Invalid credentials"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Username is already taken!"));
        }
        
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Create new user's account
        User user = User.builder()
            .username(signUpRequest.getUsername())
            .email(signUpRequest.getEmail())
            .password(passwordEncoder.encode(signUpRequest.getPassword()))
            .firstName(signUpRequest.getFirstName())
            .lastName(signUpRequest.getLastName())
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();
        
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
            .orElseGet(() -> {
                Role newRole = Role.builder()
                    .name("USER")
                    .description("Default user role")
                    .build();
                return roleRepository.save(newRole);
            });
        roles.add(userRole);
        
        user.setRoles(roles);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String username = jwtUtils.extractUsername(request.getToken());
            UserDetails userDetails = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (jwtUtils.validateToken(request.getToken(), userDetails)) {
                String newToken = jwtUtils.generateToken(userDetails);
                return ResponseEntity.ok(new JwtResponse(
                    newToken,
                    userDetails.getUsername(),
                    ((User) userDetails).getEmail(),
                    ((User) userDetails).getId()
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid refresh token"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error refreshing token"));
        }
    }
}

