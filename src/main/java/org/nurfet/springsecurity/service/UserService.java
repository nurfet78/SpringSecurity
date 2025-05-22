package org.nurfet.springsecurity.service;

import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> findAllUsers();

    UserDto findUserDtoById(Long id);

    UserDto createUserFromDto(UserDto userDto);

    UserDto updateUserFromDto(UserDto userDto);

    void deleteUserById(Long id);

    boolean existsByUsername(String username);

    User findByUsername(String username);

    UserDto userToUserDto(User user);

    boolean validateUSerData(UserDto userDto);

    /**
     * Добавить роль пользователю
     * @param userId идентификатор пользователя
     * @param roleName название роли
     * @return обновленный пользователь в виде DTO
     */
    UserDto addRoleToUser(Long userId, String roleName);

    /**
     * Удалить роль у пользователя
     * @param userId идентификатор пользователя
     * @param roleName название роли
     * @return обновленный пользователь в виде DTO
     */
    UserDto removeRoleFromUser(Long userId, String roleName);
}
