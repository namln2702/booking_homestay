package org.example.do_an_v1.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.example.do_an_v1.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// Dung cho ca xac thuc tai khoan, quen mat khau
public class ConfirmEmail extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "code")
    private String code;

    @Column(name = "expired_at")
    private LocalDateTime expired_at;

}
