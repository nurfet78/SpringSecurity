package org.nurfet.springsecurity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.LoginRequest;
import org.nurfet.springsecurity.dto.RegisterDto;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        String refreshToken = tokenRequest.get("refreshToken");
        Map<String, Object> response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto) {

        UserDto createdUser = authService.register(registerDto);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Пользователь успешно зарегистрирован");
        response.put("userId", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        authService.logout(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Выход выполнен успешно");
        return ResponseEntity.ok(response);
    }
}
