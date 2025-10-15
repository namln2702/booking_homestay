package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    /**
     * Map từ Customer Entity sang CustomerDTO
     * Con thiếu các Entity liên quan đến Customer
     */
    public static CustomerDTO customerMapCustomerDTO(Customer customer) {
        if (customer == null) return null;

        User user = customer.getUser();

        return CustomerDTO.builder()
                .idCustomer(customer.getId())
                .idUser(user != null ? user.getId() : null)
                .status(customer.getStatus())
                .dateOfBirth(customer.getDateOfBirth())
                .qrCodeUrl(customer.getQrCodeUrl())
                .lastBooking(customer.getLastBooking())
                .role(customer.getRole())
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .isOnline(user != null ? user.getIsOnline() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .age(user != null ? user.getAge() : null)
                .name(user != null ? user.getName() : null)
                .googleId(user != null ? user.getGoogleId() : null)
                .build();
    }

    /**
     * Map từ CustomerDTO sang Customer Entity
     */
//    public static Customer customerDTOMapCustomer(CustomerDTO dto) {
//        if (dto == null) return null;
//
//        // Tạo User entity từ dữ liệu DTO
//        User user = User.builder()
//                .id(dto.getIdUser())
//                .username(dto.getUsername())
//                .email(dto.getEmail())
//                .phone(dto.getPhone())
//                .isOnline(dto.getIsOnline())
//                .avatarUrl(dto.getAvatarUrl())
//                .age(dto.getAge())
//                .name(dto.getName())
//                .googleId(dto.getGoogleId())
//                .build();
//
//        // Tạo Customer entity
//        return Customer.builder()
//                .id(dto.getIdCustomer())
//                .status(dto.getStatus())
//                .dateOfBirth(dto.getDateOfBirth())
//                .qrCodeUrl(dto.getQrCodeUrl())
//                .lastBooking(dto.getLastBooking())
//                .role(dto.getRole())
//                .user(user)
//                .build();
//    }
}