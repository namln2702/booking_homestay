package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.enums.StatusHost;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostStatusUpdateRequest {

    @NotNull(message = "Host ID is required")
    private Long idHost;

    @NotNull(message = "StatusHost is required")
    private StatusHost statusHost;
}

