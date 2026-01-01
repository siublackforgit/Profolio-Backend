package com.rickyyeung.profolio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
@Data

public abstract class Base {

    @CreatedDate
    @Column(name = "createdDate", nullable = false)
    private LocalDateTime  createdDate;

    @Column(name = "createdBy", nullable = false)
    private int createdBy;

    @LastModifiedDate
    @Column(name = "lastUpdatedDate", nullable = false)
    private LocalDateTime  lastUpdatedDate;

    @Column(name = "lastUpdatedBy", nullable = false)
    private int lastUpdatedBy;

    @Column(name = "isDeleted", nullable = false)
    private boolean isDeleted = false;

}
