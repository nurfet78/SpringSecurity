package org.nurfet.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.UserDto;
import org.nurfet.springsecurity.exception.NotFoundException;
import org.nurfet.springsecurity.model.Role;
import org.nurfet.springsecurity.model.User;
import org.nurfet.springsecurity.repository.RoleRepository;
import org.nurfet.springsecurity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::userToUserDto)
                .collect(Collectors.toList());
    }

    private User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(User.class, id));
    }

    @Override
    public UserDto findUserDtoById(Long id) {
        return userToUserDto(findUserById(id));
    }

    @Override
    @Transactional
    public UserDto createUserFromDto(UserDto userDto) {
        User user = getUser(userDto, new User());
        return userToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserFromDto(UserDto userDto) {
        User user = getUser(userDto, findUserById(userDto.getId()));
        return userToUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException(
                String.format("Имя пользователя %s не найдено", username)));
    }

    @Override
    public UserDto userToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toSet()));
    }

    @Override
    public boolean validateUSerData(UserDto userDto) {
        User existingUser = findUserById(userDto.getId());
        String newUsername = userDto.getUsername();

        return existingUser.getUsername().equals(newUsername) || !existsByUsername(newUsername);
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findRoleByAuthority(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private User getUser(UserDto userDto, User user) {
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());

        if(userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        user.removeRole();
        for (String roleName : userDto.getRoles()) {
            Role role = getOrCreateRole(roleName);
            user.addRole(role);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDto addRoleToUser(Long userId, String roleName) {
        User user = findUserById(userId);
        Role role = getOrCreateRole(roleName);

        // Проверяем, есть ли уже такая роль у пользователя
        if (user.getRoles().stream().noneMatch(r -> r.getAuthority().equals(roleName))) {
            user.addRole(role);
            userRepository.save(user);
        }

        return userToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto removeRoleFromUser(Long userId, String roleName) {
        User user = findUserById(userId);

        // Находим роль, которую нужно удалить
        user.getRoles().stream()
                .filter(role -> role.getAuthority().equals(roleName))
                .findFirst()
                .ifPresent(user::removeRole);

        // Проверяем, что у пользователя осталась хотя бы одна роль
        if (user.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Пользователь должен иметь хотя бы одну роль");
        }

        userRepository.save(user);
        return userToUserDto(user);
    }
}
