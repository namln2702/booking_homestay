package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.do_an_v1.enums.StatusHost;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HostRegistrationRequest extends UserRegistrationRequest {

    private StatusHost statusHost;

    @Size(max = 255, message = "Business name must be at most 255 characters")
    private String businessName;

    @Size(max = 512, message = "QR code URL must be at most 512 characters")
    private String qrCodeUrl;
}
