package org.nurfet.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserService userService;

    /**
     * Добавить роль пользователю
     *
     * @param userId идентификатор пользователя
     * @param roleName название роли
     * @return обновленный пользователь
     */
    @PutMapping("/{userId}/roles/{roleName}")
    public ResponseEntity<UserDto> addRoleToUser(@PathVariable Long userId, @PathVariable String roleName) {
        UserDto updatedUser = userService.addRoleToUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Удалить роль у пользователя
     *
     * @param userId идентификатор пользователя
     * @param roleName название роли
     * @return обновленный пользователь
     */
    @DeleteMapping("/{userId}/roles/{roleName}")
    public ResponseEntity<UserDto> removeRoleFromUser(@PathVariable Long userId, @PathVariable String roleName) {
        UserDto updatedUser = userService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }
}
