package org.nurfet.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.dto.*;
import org.nurfet.springsecurity.exception.NotFoundException;
import org.nurfet.springsecurity.model.Role;
import org.nurfet.springsecurity.model.User;
import org.nurfet.springsecurity.repository.RoleRepository;
import org.nurfet.springsecurity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserDtoById(Long id) {
        return userToUserDto(findUserById(id));
    }

    @Override
    @Transactional
    public UserDto createUser(RegisterDto dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role defaultRole = getOrCreateRole(RoleName.ROLE_USER.name());
        user.addRole(defaultRole);

        return userToUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserDto dto) {
        User user = findUserById(id);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());

        if (dto.getRoles() != null) {
            if (dto.getRoles().isEmpty()) {
                throw new IllegalArgumentException("Пользователь должен иметь хотя бы одну роль");
            }
            user.removeRole();
            dto.getRoles().forEach(roleName -> user.addRole(getOrCreateRole(roleName)));
        }

        return userToUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Текущий пароль указан неверно");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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
    public boolean isUsernameTakenByOther(Long userId, String newUsername) {
        User existing = findUserById(userId);
        return !existing.getUsername().equals(newUsername) && existsByUsername(newUsername);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с именем '%s' не найден", username)));
    }

    @Override
    public UserDto userToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(Role::getAuthority)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    @Transactional
    public UserDto addRoleToUser(Long userId, String roleName) {
        User user = findUserById(userId);
        boolean alreadyHasRole = user.getRoles().stream()
                .anyMatch(r -> r.getAuthority().equals(roleName));

        if (!alreadyHasRole) {
            user.addRole(getOrCreateRole(roleName));
            userRepository.save(user);
        }

        return userToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto removeRoleFromUser(Long userId, String roleName) {
        User user = findUserById(userId);

        user.getRoles().stream()
                .filter(role -> role.getAuthority().equals(roleName))
                .findFirst()
                .ifPresent(user::removeRole);

        if (user.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Пользователь должен иметь хотя бы одну роль");
        }

        return userToUserDto(userRepository.save(user));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(User.class, id));
    }

    private Role getOrCreateRole(String roleName) {
        if (!RoleName.isValid(roleName)) {
            throw new IllegalArgumentException(
                    "Недопустимое имя роли: " + roleName +
                            ". Разрешены: " + Arrays.toString(RoleName.values()));
        }
        return roleRepository.findRoleByAuthority(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
