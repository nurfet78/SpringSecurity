package org.nurfet.springsecurity.security;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class JwtConfig {

    @Value("${jwt.accessSecret}")
    private String accessSecret;

    @Value("${jwt.refreshSecret}")
    private String refreshSecret;

    @Value("${jwt.accessTokenExpiration}")
    private int accessTokenExpirationInMinutes;

    @Value("${jwt.refreshTokenExpiration}")
    private int refreshTokenExpirationInMinutes;
}
