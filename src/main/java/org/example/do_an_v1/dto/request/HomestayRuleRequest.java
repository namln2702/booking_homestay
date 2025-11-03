package org.example.do_an_v1.dto.request;

import lombok.*;
import org.example.do_an_v1.enums.RuleTypeHomestay;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayRuleRequest {
    private String description;
    private RuleTypeHomestay ruleTypeHomestay;
}
