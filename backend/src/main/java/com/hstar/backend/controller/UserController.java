package com.hstar.backend.controller;

import com.hstar.backend.dto.ApiResponse;
import com.hstar.backend.dto.LoginRequest;
import com.hstar.backend.dto.SignUpUserRequest;
import com.hstar.backend.entity.User;
import com.hstar.backend.jwt.JwtUtil;
import com.hstar.backend.service.JwtBlacklistService;
import com.hstar.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        String token = jwtUtil.generateToken(loginRequest.getUsername());

        Cookie cookie = new Cookie("hstar_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(new ApiResponse<>(token, "Login successful"));
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("hstar_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @PostMapping("/logout/all")
    public void logout(
            @RequestParam(value = "requestToken", required = false) String requestToken,
            @CookieValue(value = "hstar_token", required = false) String cookieToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        String token = null;
        String bearerToken = request.getHeader("Authorization");
        if (requestToken != null) {
            token = requestToken;
        } else if (cookieToken != null) {
            token = cookieToken;
        } else if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            token = bearerToken.substring(TOKEN_PREFIX.length());
        }

        Instant instant = new Date().toInstant();
        LocalDateTime expirationTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        String username = jwtUtil.getUsernameFromToken(token);

        jwtBlacklistService.blacklistToken(token, expirationTime, username);

        Cookie cookie = new Cookie("hstar_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @PostMapping("/token/validation")
    public ResponseEntity<ApiResponse<Void>> validateToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        }
        jwtUtil.validateToken(token);
        return ResponseEntity.ok(new ApiResponse<>(null, "Token is valid"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        List<User> users = userService.getUsers();
        return ResponseEntity.ok(new ApiResponse<>(users, "Users retrieved successfully"));
    }

    @PostMapping("/signUp")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody SignUpUserRequest signUpUserRequest) {
        User user = userService.createUser(signUpUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(user, "User created successfully"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user", description = "Deletes a user by their ID")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "1")
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(null, "User deleted successfully"));
    }
}