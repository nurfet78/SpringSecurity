package org.nurfet.springsecurity.service;

import org.nurfet.springsecurity.dto.ChangePasswordRequest;
import org.nurfet.springsecurity.dto.RegisterDto;
import org.nurfet.springsecurity.dto.UpdateUserDto;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> findAllUsers();
    UserDto findUserDtoById(Long id);
    UserDto createUser(RegisterDto dto);
    UserDto updateUser(Long id, UpdateUserDto dto);
    void changePassword(Long userId, ChangePasswordRequest request);
    void deleteUserById(Long id);
    boolean existsByUsername(String username);
    boolean isUsernameTakenByOther(Long userId, String username);
    User findByUsername(String username);
    UserDto userToUserDto(User user);
    UserDto addRoleToUser(Long userId, String roleName);
    UserDto removeRoleFromUser(Long userId, String roleName);
}
