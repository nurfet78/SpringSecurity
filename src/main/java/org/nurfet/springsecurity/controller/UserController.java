package org.nurfet.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.model.User;
import org.nurfet.springsecurity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }

        User user = userService.findByUsername(username);
        UserDto userDto = userService.userToUserDto(user);
        return ResponseEntity.ok(userDto);
    }
}
