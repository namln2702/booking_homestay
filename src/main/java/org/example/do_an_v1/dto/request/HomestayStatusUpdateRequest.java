package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.enums.StatusHomestay;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomestayStatusUpdateRequest {

    @NotNull(message = "Homestay ID is required")
    private Long homestayId;

    @NotNull(message = "StatusHomestay is required")
    private StatusHomestay status;
}


