package org.nurfet.springsecurity.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JwtErrorType {

    TOKEN_EXPIRED("TOKEN_EXPIRED", "Срок действия JWT токена истек"),
    TOKEN_UNSUPPORTED("TOKEN_UNSUPPORTED", "Неподдерживаемый JWT токен"),
    TOKEN_MALFORMED("TOKEN_MALFORMED", "Неправильно сформированный JWT токен"),
    TOKEN_INVALID("TOKEN_INVALID", "Недействительный JWT токен"),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", "Ошибка аутентификации");

    private final String code;
    private final String message;
}
