package org.example.do_an_v1.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;


// Khong tao trong db, class cha chua cac truong chung
@MappedSuperclass
// Tu dong cap nhap cac gia tri @CreatedDate, @LastModifiedDate khi thao tac voi table
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;


    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDate updatedAt;



}
