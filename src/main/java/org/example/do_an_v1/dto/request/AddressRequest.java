package org.example.do_an_v1.dto.request;

import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    private String addressLine;
    private String city;
    private String state;
}
