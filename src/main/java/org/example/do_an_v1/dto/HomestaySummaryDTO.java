package org.example.do_an_v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.do_an_v1.enums.StatusHomestay;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestaySummaryDTO {

    private Long id;
    private String title;
    private String category;
    private StatusHomestay status;
    private Long hostId;
    private String hostName;
    private String city;
    private String state;
    private LocalDate createdAt;
}
