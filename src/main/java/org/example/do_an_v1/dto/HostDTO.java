package org.example.do_an_v1.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHost;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostDTO {

    // ===== Thông tin ID tu HOST va USER =====
    private Long idHost;
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

    // ===== Thông tin từ bảng Host =====
    private StatusHost statusHost;
    private String businessName;
    private String qrCodeUrl;
    private RoleUser role;

}
