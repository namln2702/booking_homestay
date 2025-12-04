package org.example.do_an_v1.dto;

import lombok.*;

/**
 * DTO cho Address (Địa chỉ)
 * 
 * Sử dụng để tránh vòng lặp khi serialize các entity có quan hệ với nhau
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

    private Long id;                    // ID của địa chỉ
    private String addressLine;         // Địa chỉ chi tiết
    private String city;                // Thành phố
    private String state;               // Tỉnh/Thành phố
    private String latitude;            // Vĩ độ
    private String longitude;           // Kinh độ
}

