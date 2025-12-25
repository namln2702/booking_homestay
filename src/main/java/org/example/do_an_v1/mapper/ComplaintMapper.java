package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.dto.TransactionDTO;
import org.example.do_an_v1.entity.Complaint;
import org.example.do_an_v1.entity.Image;
import org.example.do_an_v1.entity.Transaction;

import java.util.stream.Collectors;

public class ComplaintMapper {

    /**
     * Convert Complaint entity -> ComplaintDTO
     */
    public static ComplaintDTO toDTO(Complaint complaint) {
        return toDTO(complaint, null);
    }

    /**
     * Convert Complaint entity -> ComplaintDTO với transaction REFUND
     */
    public static ComplaintDTO toDTO(Complaint complaint, Transaction transaction) {
        if (complaint == null) {
            return null;
        }

        // Map transaction sang TransactionDTO nếu có
        TransactionDTO transactionDTO = null;
        if (transaction != null) {
            transactionDTO = TransactionMapper.toDTO(transaction);
        }

        return ComplaintDTO.builder()
                .id(complaint.getId())
                .description(complaint.getDescription())
                .createdAt(complaint.getCreatedAt())
                .adminName(complaint.getAdmin() != null 
                        && complaint.getAdmin().getUser() != null 
                        ? complaint.getAdmin().getUser().getName() 
                        : null)
                .billId(complaint.getBill() != null ? complaint.getBill().getId() : null)
                .billStatus(complaint.getBill() != null ? complaint.getBill().getStatus() : null)
                .homestayTitle(complaint.getBill() != null 
                        && complaint.getBill().getHomestay() != null 
                        ? complaint.getBill().getHomestay().getTitle() 
                        : null)
                .imageUrls(
                        complaint.getListImage() != null && !complaint.getListImage().isEmpty()
                                ? complaint.getListImage().stream()
                                .map(Image::getImage_url)
                                .filter(url -> url != null && !url.isEmpty())
                                .collect(Collectors.toList())
                                : null
                )
                .transaction(transactionDTO)
                .build();
    }
}

