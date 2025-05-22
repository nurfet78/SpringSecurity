package org.nurfet.springsecurity.exception;

import lombok.Getter;

@Getter
public class JwtAuthenticationException extends RuntimeException {
    private final JwtErrorType errorType;

    public JwtAuthenticationException(JwtErrorType errorType, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
    }

    public JwtAuthenticationException(JwtErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

}
