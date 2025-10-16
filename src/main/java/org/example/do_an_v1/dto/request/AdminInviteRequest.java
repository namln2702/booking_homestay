package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.do_an_v1.enums.LevelAdmin;

@Getter
@Setter
public class AdminInviteRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 255)
    private String fullName;

    private LevelAdmin levelAdmin;

    @Size(max = 20)
    private String phone;
}

