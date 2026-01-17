package com.rickyyeung.profolio.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "passwordHash", nullable = true)
    private String passwordHash;

    @Column(name = "googleId", nullable = true)
    private String googleId;

    @Column(name = "displayName", nullable = false)
    private String displayName;

    @Column(name = "avatarUrl", nullable = true)
    private String avatarUrl;

    @Column(name = "isEmailVerified", nullable = false)
    private Boolean isEmailVerified;

    @Column(name = "userRole", nullable = false)
    private int userRole;

}
