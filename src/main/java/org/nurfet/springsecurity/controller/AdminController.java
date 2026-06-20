package org.nurfet.springsecurity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.RegisterDto;
import org.nurfet.springsecurity.dto.UpdateUserDto;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserDtoById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody RegisterDto dto) {
        if (userService.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Имя пользователя уже используется");
        }
        UserDto created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UpdateUserDto dto) {
        if (userService.isUsernameTakenByOther(id, dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Имя пользователя уже используется");
        }
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        UserDto user = userService.findUserDtoById(id);
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message",
                "Пользователь " + user.getFullName() + " успешно удалён"));
    }
}
