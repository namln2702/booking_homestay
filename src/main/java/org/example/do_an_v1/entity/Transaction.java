package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.enums.TypeTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_transactions")
public class Transaction extends BaseEntity{

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "type", nullable = false, length = 50)
    private String type;


    // Phan loai transaction
    @Column(name = "type", length = 20)
    private TypeTransaction transactionType;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Trang thai cua transaction
    @Column(name = "status")
    private StatusTransaction status;

    @ManyToOne
    @JoinColumn(name = "from_user", nullable = false)
    User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user", nullable = false)
    User toUser;

    @ManyToOne
    @JoinColumn(name = "bill", nullable = false)
    Bill bill;

}
