package org.nurfet.springsecurity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.springsecurity.exception.JwtAuthenticationException;
import org.nurfet.springsecurity.exception.JwtErrorType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtConfig jwtConfig;

    private static final String ROLES_CLAIM = "roles";
    private static final String REFRESH_TOKEN_CLAIM = "isRefresh";

    private SecretKey jwtAccessSecret;
    private SecretKey jwtRefreshSecret;

    @PostConstruct
    public void init() {
        this.jwtAccessSecret = generateSecretKey(jwtConfig.getAccessSecret());
        this.jwtRefreshSecret = generateSecretKey(jwtConfig.getRefreshSecret());
    }

    /**
     * Генерирует ключ на основе Base64-закодированного секрета
     *
     * @param secret Base64-закодированный секрет
     * @return секретный ключ
     */
    private SecretKey generateSecretKey(String secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Создает JWT токен с указанными параметрами
     *
     * @param claims набор данных (claims) для токена
     * @param subject идентификатор пользователя (subject)
     * @param key секретный ключ для подписи
     * @param expirationMinutes время жизни токена в минутах
     * @return JWT токен
     */
    private String createToken(Map<String, Object> claims, String subject,
                               SecretKey key, int expirationMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusMinutes(expirationMinutes);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Генерирует JWT access токен на основе данных аутентификации
     *
     * @param authentication объект аутентификации
     * @return JWT токен
     */
    public String generateAccessToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put(ROLES_CLAIM, roles);

        return createToken(claims, authentication.getName(),
                jwtAccessSecret, jwtConfig.getAccessTokenExpirationInMinutes());
    }

    /**
     * Генерирует JWT refresh токен на основе данных аутентификации
     *
     * @param authentication объект аутентификации
     * @return JWT refresh токен
     */
    public String generateRefreshToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(REFRESH_TOKEN_CLAIM, true);
        return createToken(claims, authentication.getName(),
                jwtRefreshSecret, jwtConfig.getRefreshTokenExpirationInMinutes());
    }

    /**
     * Проверяет, истёк ли срок действия токена
     *
     * @param claims набор данных (claims) из токена
     * @return true если токен истёк
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Проверяет валидность токена с указанным ключом
     *
     * @param token JWT токен для проверки
     * @param key ключ для проверки подписи
     * @return true если токен валидный и не истёк
     */
    private boolean validateToken(String token, SecretKey key) {
        try {
            return !isTokenExpired(extractClaims(token, key));
        } catch (ExpiredJwtException expEx) {
            throw new JwtAuthenticationException(JwtErrorType.TOKEN_EXPIRED, expEx);
        } catch (UnsupportedJwtException unsEx) {
            throw new JwtAuthenticationException(JwtErrorType.TOKEN_UNSUPPORTED, unsEx);
        } catch (MalformedJwtException mjEx) {
            throw new JwtAuthenticationException(JwtErrorType.TOKEN_MALFORMED, mjEx);
        } catch (JwtException | ResponseStatusException sEx) {
            throw new JwtAuthenticationException(JwtErrorType.TOKEN_INVALID, sEx);
        }
    }

    /**
     * Проверяет валидность access токена
     *
     * @param token JWT токен для проверки
     * @return true если токен валидный
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token, jwtAccessSecret);
    }

    /**
     * Проверяет валидность refresh токена
     *
     * @param token JWT токен для проверки
     * @return true если токен валидный
     */
    public boolean validateRefreshToken(String token) {
        try {
            return validateToken(token, jwtRefreshSecret);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Извлекает Claims из JWT токена
     *
     * @param token JWT токен
     * @param secretKey ключ для проверки подписи
     * @return объект Claims
     */
    private Claims extractClaims(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Получает имя пользователя из refresh токена
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromRefreshToken(String token) {
        return extractClaims(token, jwtRefreshSecret).getSubject();
    }

    /**
     * Получает объект Authentication из access токена
     *
     * @param token JWT токен
     * @return объект Authentication
     */
    public Authentication getAuthentication(String token) {
        Claims claims = extractClaims(token, jwtAccessSecret);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(ROLES_CLAIM).toString().split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails userDetails = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }
}
