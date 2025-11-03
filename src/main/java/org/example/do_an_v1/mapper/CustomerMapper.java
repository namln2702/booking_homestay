package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.entity.Customer;

public class CustomerMapper {

    /**
     * Chuyển từ Entity Customer sang DTO
     */
    public static CustomerDTO toDTO(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerDTO.builder()
                .idCustomer(customer.getUser() != null ? customer.getUser().getId() : null)
                .name(customer.getUser() != null ? customer.getUser().getName() : null)
                .email(customer.getUser() != null ? customer.getUser().getEmail() : null)
                .phone(customer.getUser() != null ? customer.getUser().getPhone() : null)
                .qrCodeUrl(customer.getQrCodeUrl())
                .dateOfBirth(customer.getDateOfBirth())
                .lastBooking(customer.getLastBooking())
                .role(customer.getRole() != null ? customer.getRole() : null)
                .status(customer.getStatus() != null ? customer.getStatus() : null)
                .build();
    }

    /**
     * Chuyển từ DTO sang Entity Customer (dùng khi tạo mới / cập nhật)
     */
    public static Customer toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer entity = new Customer();
        entity.setQrCodeUrl(dto.getQrCodeUrl());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setLastBooking(dto.getLastBooking());
        // role và status sẽ được gán trong service (nếu có enum tương ứng)
        return entity;
    }
}
