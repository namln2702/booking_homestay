package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.HomestayRulesDTO;
import org.example.do_an_v1.entity.HomestayRule;

public class HomestayRulesMapper {

    public static HomestayRulesDTO toDTO(HomestayRule entity) {
        if (entity == null) return null;
        return HomestayRulesDTO.builder()
                .id(entity.getId())
                .ruleTypeHomestay(entity.getRuleTypeHomestay())
                .build();
    }

    public static HomestayRule toEntity(HomestayRulesDTO dto) {
        if (dto == null) return null;
        HomestayRule rule = new HomestayRule();
        rule.setId(dto.getId());
        rule.setRuleTypeHomestay(dto.getRuleTypeHomestay());
        return rule;
    }
}
