package org.nurfet.springsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class UserDto {

    private Long id;

    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
    @NotBlank(message = "Имя должно быть указано")
    private String firstName;

    @Size(min = 2, max = 30, message = "Фамилия должна содержать от 2 до 30 символов")
    @NotBlank(message = "Фамилия должна быть указана")
    private String lastName;

    @Pattern(regexp = "^(\\w+\\.)*\\w+@(\\w+\\.)+[A-Za-z]+$", message = "Адрес электронной почты указан неверно")
    @NotBlank(message = "Поле email должно быть заполнено")
    private String email;

    @Size(min = 3, max = 30, message = "Имя пользователя должно содержать от 3 до 30 символов")
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String username;

    @Size(min = 4, max = 16, message = "Пароль должен содержать от 4 до 16 символов")
    @NotBlank(message = "Пароль не должно быть пустым")
    private String password;

    @Size(min = 1, message = "Должна быть выбрана хотя бы одна роль")
    private Set<String> roles = new HashSet<>();

    public UserDto(Long id, String firstName, String lastName, String email, String username, Set<String> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.roles = roles;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
