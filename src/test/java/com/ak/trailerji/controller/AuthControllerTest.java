package com.ak.trailerji.controller;

import com.ak.trailerji.controller.AuthController;
import com.ak.trailerji.dto.JwtResponse;
import com.ak.trailerji.dto.LoginRequest;
import com.ak.trailerji.dto.RegisterRequest;
import com.ak.trailerji.entity.Role;
import com.ak.trailerji.entity.User;
import com.ak.trailerji.repository.RoleRepository;
import com.ak.trailerji.repository.UserRepository;
import com.ak.trailerji.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@org.springframework.test.context.ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        // Setup test role
        testRole = Role.builder()
                .id(1L)
                .name("USER")
                .description("Default user role")
                .build();

        // Setup test user
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();

        // Setup login request
        loginRequest = new LoginRequest("testuser", "password123");

        // Setup register request
        registerRequest = new RegisterRequest(
                "newuser",
                "newuser@example.com",
                "password123",
                "New",
                "User"
        );

        // Setup refresh token request
        refreshTokenRequest = new RefreshTokenRequest("valid-jwt-token");
    }

    // =====================================================
    // LOGIN ENDPOINT TESTS
    // =====================================================

    @Test
    @WithMockUser
    void testLoginSuccess() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateToken(testUser)).thenReturn("generated-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("generated-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.id").value(1));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtils).generateToken(testUser);
    }

    @Test
    @WithMockUser
    void testLoginWithInvalidCredentials() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid credentials"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @WithMockUser
    void testLoginWithAuthenticationException() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication error"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid credentials"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @WithMockUser
    void testLoginWhenUserNotFoundAfterAuthentication() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid credentials"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @WithMockUser
    void testLoginWithValidationErrors() throws Exception {
        // Arrange - create invalid login request with blank username
        LoginRequest invalidRequest = new LoginRequest("", "pass");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // =====================================================
    // REGISTER ENDPOINT TESTS
    // =====================================================

    @Test
    @WithMockUser
    void testRegisterSuccess() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser
    void testRegisterWithExistingUsername() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser
    void testRegisterWithExistingEmail() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser
    void testRegisterWhenRoleDoesNotExist() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("USER");
        verify(roleRepository).save(any(Role.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser
    void testRegisterWithValidationErrors() throws Exception {
        // Arrange - create invalid register request with short username
        RegisterRequest invalidRequest = new RegisterRequest(
                "ab",  // too short (min 3)
                "invalid-email",  // invalid email format
                "pass",  // too short (min 6)
                "First",
                "Last"
        );

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser
    void testRegisterWithAllOptionalFields() throws Exception {
        // Arrange - register request already has firstName and lastName
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify firstName and lastName are set
            assert savedUser.getFirstName().equals("New");
            assert savedUser.getLastName().equals("User");
            return savedUser;
        });

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(userRepository).save(any(User.class));
    }

    // =====================================================
    // REFRESH TOKEN ENDPOINT TESTS
    // =====================================================

    @Test
    @WithMockUser
    void testRefreshTokenSuccess() throws Exception {
        // Arrange
        when(jwtUtils.extractUsername("valid-jwt-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.validateToken("valid-jwt-token", testUser)).thenReturn(true);
        when(jwtUtils.generateToken(testUser)).thenReturn("new-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.id").value(1));

        verify(jwtUtils).extractUsername("valid-jwt-token");
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtils).validateToken("valid-jwt-token", testUser);
        verify(jwtUtils).generateToken(testUser);
    }

    @Test
    @WithMockUser
    void testRefreshTokenWithInvalidToken() throws Exception {
        // Arrange
        when(jwtUtils.extractUsername("valid-jwt-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.validateToken("valid-jwt-token", testUser)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        verify(jwtUtils).extractUsername("valid-jwt-token");
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtils).validateToken("valid-jwt-token", testUser);
        verify(jwtUtils, never()).generateToken(any());
    }

    @Test
    @WithMockUser
    void testRefreshTokenWithNonExistentUser() throws Exception {
        // Arrange
        when(jwtUtils.extractUsername("valid-jwt-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error refreshing token"));

        verify(jwtUtils).extractUsername("valid-jwt-token");
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtils, never()).validateToken(anyString(), any());
    }

    @Test
    @WithMockUser
    void testRefreshTokenWithExceptionDuringExtraction() throws Exception {
        // Arrange
        when(jwtUtils.extractUsername("valid-jwt-token"))
                .thenThrow(new RuntimeException("Invalid token format"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error refreshing token"));

        verify(jwtUtils).extractUsername("valid-jwt-token");
        verify(userRepository, never()).findByUsername(anyString());
    }
}
