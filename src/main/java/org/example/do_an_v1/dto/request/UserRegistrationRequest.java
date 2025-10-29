package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @Size(max = 255, message = "Username must be at most 255 characters")
    private String username;

    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Min(value = 0, message = "Age must be positive")
    private Integer age;

    @Size(max = 512, message = "Avatar URL must be at most 512 characters")
    private String avatarUrl;
}
