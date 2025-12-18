package com.rickyyeung.profolio.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User extends base{
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

}
