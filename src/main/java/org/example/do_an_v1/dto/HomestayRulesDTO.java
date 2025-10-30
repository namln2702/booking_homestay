package org.example.do_an_v1.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.do_an_v1.enums.RuleTypeHomestay;


@Builder
@Getter
@Setter
public class HomestayRulesDTO {

    private Long id;

    private String description;

    private RuleTypeHomestay ruleTypeHomestay ;
}
