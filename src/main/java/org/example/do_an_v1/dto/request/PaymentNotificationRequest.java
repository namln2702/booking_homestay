package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNotificationRequest {
    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    @NotNull(message = "Success status is required")
    private Boolean success;

    private String message; // Optional message about payment result
}

