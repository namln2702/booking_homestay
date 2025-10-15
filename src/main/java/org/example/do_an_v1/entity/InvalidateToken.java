package org.example.do_an_v1.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "tbl_invalidateTokens")
public class InvalidateToken {

    @Id
    private String tokenId;

    private Date expired;
}
