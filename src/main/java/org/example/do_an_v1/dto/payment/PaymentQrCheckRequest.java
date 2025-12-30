package org.example.do_an_v1.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentQrCheckRequest {

    @NotBlank(message = "imageUrl is required")
    private String imageUrl;
}
