package org.nurfet.springsecurity.security;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Получаем информацию об ошибке из атрибутов запроса
        String errorCode = (String) request.getAttribute("jwt.error.code");
        String errorMessage = (String) request.getAttribute("jwt.error.message");

        // Если атрибуты не установлены, используем значения по умолчанию
        if (errorCode == null) {
            errorCode = "UNAUTHORIZED";
            errorMessage = "Необходима аутентификация";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // Формируем JSON ответ
        String jsonResponse = createErrorResponse(errorCode, errorMessage, request.getRequestURI());
        response.getWriter().write(jsonResponse);
    }

    private String createErrorResponse(String code, String message, String path) {
        return String.format(
                "{\"error\":{\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\",\"path\":\"%s\"}}",
                code, message, Instant.now().toString(), path
        );
    }
}
