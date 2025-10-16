package org.example.do_an_v1.dto;



import lombok.*;
import org.example.do_an_v1.enums.LevelAdmin;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDTO {

    // ===== Thông tin từ ID cua ADMIN va USER =====
    private Long idAdmin;
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

    // ===== Thông tin từ bảng Admin =====
    private LevelAdmin levelAdmin;
    private Status status;
    private RoleUser role;

}

