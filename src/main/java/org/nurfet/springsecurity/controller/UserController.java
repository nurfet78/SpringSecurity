package org.nurfet.springsecurity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.ChangePasswordRequest;
import org.nurfet.springsecurity.dto.UpdateUserDto;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.model.User;
import org.nurfet.springsecurity.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(userService.userToUserDto(user));
    }

    @PutMapping
    public ResponseEntity<UserDto> updateCurrentUser(@Valid @RequestBody UpdateUserDto dto) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);

        if (userService.isUsernameTakenByOther(user.getId(), dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Имя пользователя уже используется");
        }

        // Роли пользователь сам не меняет — передаём null
        dto.setRoles(null);
        return ResponseEntity.ok(userService.updateUser(user.getId(), dto));
    }

    @PatchMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменён"));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) return ud.getUsername();
        if (principal instanceof String s) return s;
        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }
}
