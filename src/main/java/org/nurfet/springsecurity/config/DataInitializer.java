package org.nurfet.springsecurity.config;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.model.Role;
import org.nurfet.springsecurity.model.User;
import org.nurfet.springsecurity.repository.RoleRepository;
import org.nurfet.springsecurity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        Role roleAdmin = addRole("ROLE_ADMIN");
        Role roleUser = addRole("ROLE_USER");

        addDefaultUser("Александр", "Александров", "alex@mail.ru",
                "admin", roleAdmin, roleUser);

        addDefaultUser("Марина", "Маринина", "marina@mail.ru",
                "user", roleUser);
    }

    private Role addRole(String roleName) {
        return roleRepository.findRoleByAuthority(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private void addDefaultUser(String firstName, String lastName, String email,
                                String username, Role... roles) {

        userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User(
                    firstName,
                    lastName,
                    email,
                    username,
                    passwordEncoder.encode(username),
                    new HashSet<>(Arrays.asList(roles))
            );

            return userRepository.save(user);
        });
    }
}
