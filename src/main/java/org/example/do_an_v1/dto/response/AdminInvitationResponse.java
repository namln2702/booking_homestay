package org.example.do_an_v1.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.do_an_v1.dto.AdminDTO;

@Getter
@Builder
public class AdminInvitationResponse {

    private final AdminDTO admin;
    private final String activationCode;
}

