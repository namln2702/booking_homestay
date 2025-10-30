package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.do_an_v1.enums.Status;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomerRegistrationRequest extends UserRegistrationRequest {

    private Status status;

    @Size(max = 255, message = "Date of birth must be at most 255 characters")
    private String dateOfBirth;

    @Size(max = 512, message = "QR code URL must be at most 512 characters")
    private String qrCodeUrl;

    @Size(max = 255, message = "Last booking must be at most 255 characters")
    private String lastBooking;
}
