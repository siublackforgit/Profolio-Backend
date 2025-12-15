package com.rickyyeung.profolio.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long projectId;
    private Long userId;
    private String projectTitle;
    private String projectDescription;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedBy;
}
