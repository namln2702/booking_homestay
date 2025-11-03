package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.entity.Transaction;

public class TransactionMapper {

    /**
     * Chuyển từ Entity Transaction sang DTO TransactionDTO
     */
    public static TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionDTO.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .completedAt(transaction.getCompletedAt())

                // Thông tin người gửi
                .fromUserId(transaction.getFromUser() != null ? transaction.getFromUser().getId() : null)
                .fromUserEmail(transaction.getFromUser() != null ? transaction.getFromUser().getEmail() : null)

                // Thông tin người nhận
                .toUserId(transaction.getToUser() != null ? transaction.getToUser().getId() : null)
                .toUserEmail(transaction.getToUser() != null ? transaction.getToUser().getEmail() : null)

                // Thông tin hóa đơn
                .billId(transaction.getBill() != null ? transaction.getBill().getId() : null)
                .billCode(transaction.getBill() != null ? transaction.getBill().getCode() : null)
                .build();
    }

    /**
     * Chuyển từ DTO sang Entity Transaction (dùng khi tạo mới giao dịch)
     */
    public static Transaction toEntity(TransactionDTO dto) {
        if (dto == null) {
            return null;
        }

        Transaction entity = new Transaction();
        entity.setId(dto.getId());
        entity.setAmount(dto.getAmount());
        entity.setTransactionType(dto.getTransactionType());
        entity.setStatus(dto.getStatus());
        entity.setCompletedAt(dto.getCompletedAt());
        // Các quan hệ fromUser, toUser, bill sẽ được set ở service
        return entity;
    }
}