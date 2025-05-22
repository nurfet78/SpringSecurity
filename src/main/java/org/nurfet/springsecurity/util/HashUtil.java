package org.nurfet.springsecurity.util;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.security.JwtConfig;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class HashUtil {

    private final JwtConfig jwtConfig;

    public String hmacSha512(String value) {
        try {
            String secret = jwtConfig.getAccessSecret();
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации HMAC", e);
        }
    }
}
