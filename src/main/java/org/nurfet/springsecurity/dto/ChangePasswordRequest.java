package org.nurfet.springsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Текущий пароль не должен быть пустым")
    private String currentPassword;

    @Size(min = 4, max = 16, message = "Пароль должен содержать от 4 до 16 символов")
    @NotBlank(message = "Новый пароль не должен быть пустым")
    private String newPassword;

    @NotBlank(message = "Подтверждение пароля не должно быть пустым")
    private String confirmPassword;
}
