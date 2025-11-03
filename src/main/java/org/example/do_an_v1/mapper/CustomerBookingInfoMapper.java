package org.example.do_an_v1.mapper;


import org.example.do_an_v1.dto.CustomerBookingInfoDTO;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.CustomerBookingInfo;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerBookingInfoMapper {

    /**
     * Chuyển từ Entity sang DTO
     */
    public static CustomerBookingInfoDTO toDTO(CustomerBookingInfo entity) {
        if (entity == null) {
            return null;
        }

        return CustomerBookingInfoDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .specialRequires(entity.getSpecialRequires())

                // Map danh sách bill (nếu có)
//                .bills(toBillDTOList(entity.getListBill()))
                .build();
    }

    /**
     * Chuyển từ DTO sang Entity (dùng khi tạo mới hoặc cập nhật)
     */
    public static CustomerBookingInfo toEntity(CustomerBookingInfoDTO dto) {
        if (dto == null) {
            return null;
        }

        CustomerBookingInfo entity = new CustomerBookingInfo();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setSpecialRequires(dto.getSpecialRequires());
        // listBill được gán ở Service nếu cần
        return entity;
    }

    /**
     * Chuyển danh sách Bill entity sang BillDTO
     */
//    private static List<BillDTO> toBillDTOList(Set<Bill> bills) {
//        if (bills == null || bills.isEmpty()) {
//            return null;
//        }
//        return bills.stream()
//                .map(BillMapper::toDTO)
//                .collect(Collectors.toList());
//    }
}
