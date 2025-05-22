package org.nurfet.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.exception.NotFoundException;
import org.nurfet.springsecurity.model.RefreshToken;
import org.nurfet.springsecurity.model.User;
import org.nurfet.springsecurity.repository.RefreshTokenRepository;
import org.nurfet.springsecurity.repository.UserRepository;
import org.nurfet.springsecurity.security.JwtConfig;
import org.nurfet.springsecurity.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final JwtConfig jwtConfig;

    private final HashUtil hashUtil;


    @Override
    public Optional<RefreshToken> findByToken(String token) {
        String tokenHash = hashUtil.hmacSha512(token);
        return refreshTokenRepository.findByToken(tokenHash);
    }

    @Override
    public boolean verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            return false;
        }

        return true;
    }

    @Override
    public RefreshToken saveRefreshToken(String username, String refreshToken) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            RefreshToken existingToken = user.getRefreshToken();

            final LocalDateTime now = LocalDateTime.now();
            final Instant issuedAtInstant = now.atZone(ZoneId.systemDefault()).toInstant();
            final Instant expirationTime = now.plusMinutes(jwtConfig.getRefreshTokenExpirationInMinutes())
                    .atZone(ZoneId.systemDefault()).toInstant();

            String tokenHash = hashUtil.hmacSha512(refreshToken);

            if (existingToken != null) {
                existingToken.setToken(tokenHash);
                existingToken.setExpirationTime(expirationTime);
                existingToken.setCreationTime(issuedAtInstant);
                existingToken.setUsername(username);
                return refreshTokenRepository.save(existingToken);
            } else {
                RefreshToken newToken = new RefreshToken(tokenHash, username,
                        expirationTime, issuedAtInstant, user);
                return refreshTokenRepository.save(newToken);
            }
        } else {
            throw new NotFoundException(String.format("Пользователь с именем пользователя %s не найден", username));
        }
    }

    @Transactional
    @Override
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    @Transactional
    @Override
    public void deleteByToken(String token) {
        String tokenHash = hashUtil.hmacSha512(token);
        refreshTokenRepository.deleteByToken(tokenHash);
    }
}
