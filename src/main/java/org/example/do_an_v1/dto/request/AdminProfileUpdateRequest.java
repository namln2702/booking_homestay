package org.example.do_an_v1.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.do_an_v1.enums.LevelAdmin;
import org.example.do_an_v1.enums.Status;

@Getter
@Setter
public class AdminProfileUpdateRequest {

    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone; // TODO: need to check Vietnamese phone number

    @Min(value = 0, message = "Age must be positive")
    private Integer age;

    @Size(max = 512, message = "Avatar URL must be at most 512 characters")
    private String avatarUrl;

    private LevelAdmin levelAdmin;

    private Status status;
}
