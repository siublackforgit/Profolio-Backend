package com.rickyyeung.profolio.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "jwtToken")
public class JwtToken extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jwtTokenId;

    @Column(name = "jwtTokenJti", nullable = false, unique = true)
    private String jwtTokenJti;

    @Column(name = "jwtTokenUserId", nullable = false)
    private Long jwtTokenUserId;

    @Column(name = "jwtTokenHashedToken", nullable = false)
    private Long jwtTokenHashedToken;

    @Column(name = "jwtTokenIsRevoke", nullable = false)
    private Boolean jwtTokenIsRevoke;

    @Column(name = "jwtTokenExpiredAt", nullable = false)
    private LocalDateTime jwtTokenExpiredAt;
}
