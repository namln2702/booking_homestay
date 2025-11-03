package org.example.do_an_v1.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBookingInfoDTO {

    private Long id;                  // ID của bản ghi (kế thừa từ BaseEntity)
    private String name;              // Tên khách đặt
    private String email;             // Email liên hệ
    private String phoneNumber;       // Số điện thoại (unique)
    private String specialRequires;   // Yêu cầu đặc biệt (nếu có)

}
