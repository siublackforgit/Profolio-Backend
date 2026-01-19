package com.rickyyeung.profolio.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "loginlog")
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loginLogId;

    @Column(name = "loginLogUserId", nullable = false)
    private Long loginLogUserId;

    @Column(name = "loginLogIpAddress", nullable = false)
    private String loginLogIpAddress;

    @CreatedDate
    @Column(name = "loginLogLoginTime", nullable = false)
    private LocalDateTime loginLogLoginTime;
}
