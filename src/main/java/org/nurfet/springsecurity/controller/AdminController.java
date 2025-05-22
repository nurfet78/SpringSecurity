package org.nurfet.springsecurity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.of(Optional.ofNullable(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.findUserDtoById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {

        if (userService.existsByUsername(userDto.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Имя пользователя уже используется");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        UserDto createUser = userService.createUserFromDto(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUser);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {

        userDto.setId(id);

        if (!userService.validateUSerData(userDto)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Имя пользователя уже используется");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        UserDto updateUser = userService.updateUserFromDto(userDto);
        return ResponseEntity.ok(updateUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        UserDto user = userService.findUserDtoById(id);
        userService.deleteUserById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Пользователь " + user.getFullName() + " успешно удален");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
