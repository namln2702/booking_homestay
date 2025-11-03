package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostRegistrationRequest {

    @Size(max = 50, message = "Username must be at most 50 characters")
    private String username;

    @Size(max = 100, message = "Name must be at most 100 characters")
    @NotBlank(message = "Name is required")
    private String name;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    @NotBlank(message = "Phone is required")
    private String phone;

    private Integer age;

    private String avatarUrl;

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String qrCodeUrl;
}
