package org.nurfet.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.LoginRequest;
import org.nurfet.springsecurity.dto.RegisterDto;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.exception.NotFoundException;
import org.nurfet.springsecurity.model.RefreshToken;
import org.nurfet.springsecurity.security.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final JwtTokenUtil jwtTokenUtil;

    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager authenticationManager;

    public Map<String, Object> login(LoginRequest loginRequest) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                        loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);

        String accessToken = jwtTokenUtil.generateAccessToken(authentication);
        String refreshToken = jwtTokenUtil.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(loginRequest.getUsername(), refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Вход выполнен успешно");
        response.put("username", loginRequest.getUsername());
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");

        return response;
    }

    // Обновление токена с помощью refresh токена
    public Map<String, Object> refreshToken(String refreshToken) {

        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh токен не предоставлен");
        }

        if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
            // Можно выбросить более специфичное исключение или обработать как есть
            throw new IllegalArgumentException("Представленный refresh токен не является валидным JWT.");
        }

        Optional<RefreshToken> storedToken = refreshTokenService.findByToken(refreshToken);

        if (storedToken.isPresent() && refreshTokenService.verifyExpiration(storedToken.get())) {

            String username = storedToken.get().getUsername();
            UserDetails userDetails = userService.findByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());

            String newAccessToken = jwtTokenUtil.generateAccessToken(authentication);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(authentication);

            refreshTokenService.deleteByToken(refreshToken);

            refreshTokenService.saveRefreshToken(username, newRefreshToken);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken);
            response.put("tokenType", "Bearer");

            return response;
        } else {
            storedToken.ifPresent(token -> refreshTokenService.deleteByUsername(token.getUsername()));

            throw new NotFoundException("Недействительный refresh токен");
        }
    }

    public void logout(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Имя пользователя не предоставлено");
        }

        refreshTokenService.deleteByUsername(username);
    }

    public UserDto register(RegisterDto registerDto) {

        if (userService.existsByUsername(registerDto.getUsername())) {
            throw new IllegalArgumentException("Имя пользователя уже используется");
        }
        UserDto userDto = new UserDto();
        userDto.setFirstName(registerDto.getFirstName());
        userDto.setLastName(registerDto.getLastName());
        userDto.setEmail(registerDto.getEmail());
        userDto.setUsername(registerDto.getUsername());
        userDto.setPassword(registerDto.getPassword());
        userDto.setRoles(Collections.singleton("ROLE_USER"));
        return userService.createUserFromDto(userDto);
    }
}
