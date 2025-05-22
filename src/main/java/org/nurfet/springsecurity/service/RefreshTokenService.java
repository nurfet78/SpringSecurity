package org.nurfet.springsecurity.service;

import org.nurfet.springsecurity.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> findByToken(String token);

    boolean verifyExpiration(RefreshToken token);

    RefreshToken saveRefreshToken(String username, String refreshToken);

    void deleteByUsername(String username);

    void deleteByToken(String token);
}
