package org.nurfet.springsecurity.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.springsecurity.exception.JwtAuthenticationException;
import org.nurfet.springsecurity.exception.JwtErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenUtil.validateAccessToken(jwt)) {
                Authentication authentication = jwtTokenUtil.getAuthentication(jwt);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Установлена аутентификация в SecurityContext для '{}', url: {}",
                        authentication.getName(), request.getRequestURL());
            }
        } catch (JwtAuthenticationException e) {
            log.error("JWT authentication error: {} - {}", e.getErrorType().getCode(), e.getMessage());
            setErrorAttributes(request, e.getErrorType());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("Unexpected authentication error: {}", e.getMessage(), e);
            setErrorAttributes(request, JwtErrorType.AUTHENTICATION_ERROR);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void setErrorAttributes(HttpServletRequest request, JwtErrorType errorType) {
        request.setAttribute("jwt.error.code", errorType.getCode());
        request.setAttribute("jwt.error.message", errorType.getMessage());
    }
}
