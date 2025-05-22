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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

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

    private Set<String> roles = new HashSet<>();
}
