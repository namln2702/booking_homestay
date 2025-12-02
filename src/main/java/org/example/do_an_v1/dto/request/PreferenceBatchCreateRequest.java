package org.example.do_an_v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.do_an_v1.dto.PreferenceDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceBatchCreateRequest {

    @NotEmpty(message = "Preferences list cannot be empty")
    @Size(max = 50, message = "Cannot create more than 50 preferences at once")
    @Valid
    private List<PreferenceDTO> preferences;
}

