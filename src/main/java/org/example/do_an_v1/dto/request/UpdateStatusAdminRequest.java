package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.do_an_v1.enums.Status;

@Getter
@Setter
public class UpdateStatusAdminRequest {

    @NotNull
    private Long idAdmin;

    @NotNull
    private Status status;
}

