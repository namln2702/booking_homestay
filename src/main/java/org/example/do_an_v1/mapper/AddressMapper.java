package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.AddressDTO;
import org.example.do_an_v1.entity.Address;

/**
 * Mapper để chuyển đổi giữa Address entity và AddressDTO
 */
public class AddressMapper {

    /**
     * Chuyển từ Entity Address sang DTO AddressDTO
     */
    public static AddressDTO toDTO(Address address) {
        if (address == null) {
            return null;
        }

        return AddressDTO.builder()
                .id(address.getId())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    /**
     * Chuyển từ DTO AddressDTO sang Entity Address
     */
    public static Address toEntity(AddressDTO dto) {
        if (dto == null) {
            return null;
        }

        Address address = Address.builder()
                .addressLine(dto.getAddressLine())
                .city(dto.getCity())
                .state(dto.getState())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
        
        // Set id nếu có (khi cập nhật)
        if (dto.getId() != null) {
            address.setId(dto.getId());
        }
        
        return address;
    }
}

