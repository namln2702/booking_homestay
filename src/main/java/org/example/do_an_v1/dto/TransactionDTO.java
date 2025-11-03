package org.example.do_an_v1.dto;

import lombok.*;
import org.example.do_an_v1.enums.StatusTransaction;
import org.example.do_an_v1.enums.TypeTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {

    private Long id;                        // ID của transaction
    private BigDecimal amount;              // Số tiền giao dịch
    private TypeTransaction transactionType; // Loại giao dịch (VD: PAYMENT, REFUND, DEPOSIT)
    private StatusTransaction status;       // Trạng thái giao dịch (PENDING, SUCCEED, FAILED)
    private LocalDateTime completedAt;      // Thời điểm hoàn tất

    // Thông tin liên quan đến người gửi - nhận
    private Long fromUserId;
    private String fromUserEmail;

    private Long toUserId;
    private String toUserEmail;

    // Thông tin liên kết Bill
    private Long billId;
    private String billCode; // nếu Bill có trường code
}
