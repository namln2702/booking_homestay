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

    // Phan loai transaction
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TypeTransaction transactionType;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Trang thai cua transaction
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusTransaction status;

    @ManyToOne
    @JoinColumn(name = "from_user", nullable = false)
    User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user", nullable = true)
    User toUser;

    @ManyToOne
    @JoinColumn(name = "bill", nullable = false)
    Bill bill;

    @Column(name = "order_id", unique = true)
    private String orderId;  // VNPay orderId (vnp_TxnRef) để tra cứu khi callback

    @Column(name = "proof_image_url")
    private String proofImageUrl;  // URL hình ảnh chứng minh giao dịch (cho refund)

}
