package org.example.do_an_v1.dto;



import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.entity.Preference;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    // ===== Thông tin ID tu Customer va User =====
    private Long idCustomer;
    private Long idUser;

    // ===== Thông tin từ bảng User =====
    private String username;
    private String email;
    private String phone;
    private Boolean isOnline;
    private String avatarUrl;
    private Integer age;
    private String name;
    private String googleId;
    private List<Long> listPreference;

    // ===== Thông tin từ bảng Customer =====
    private Status status;
    private String dateOfBirth;
    private String qrCodeUrl;
    private String lastBooking;
    private RoleUser role;

}
