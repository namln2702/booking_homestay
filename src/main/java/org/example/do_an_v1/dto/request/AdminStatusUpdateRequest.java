package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.enums.Status;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatusUpdateRequest {

    @NotNull(message = "Admin ID is required")
    private Long idAdmin;

    @NotNull(message = "Status is required")
    private Status status;
}

