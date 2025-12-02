package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinRequest {
    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotBlank(message = "Check-in code is required")
    private String code;
}

