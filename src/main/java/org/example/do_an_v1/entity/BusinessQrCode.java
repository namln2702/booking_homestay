package org.example.do_an_v1.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.do_an_v1.enums.Status;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "tbl_bussiness_qr_code")
public class BusinessQrCode extends BaseEntity{

    @Column(name = "qr_code_url", nullable = false)
    private String qrCodeUrl;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "card_password", nullable = false)
    private String card_password ;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_short_name", nullable = false)
    private String bankShortName;

}
