package org.nurfet.springsecurity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken extends AbstractEntity {

    @Serial
    private static final long serialVersionUID = 4L;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Instant expirationTime;

    @Column(nullable = false)
    private Instant creationTime;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }
}
