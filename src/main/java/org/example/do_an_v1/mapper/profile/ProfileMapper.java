package org.example.do_an_v1.mapper.profile;

import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public CustomerDTO toCustomerDTO(Customer customer) {
        User user = customer.getUser();

        Long userId = user != null ? user.getId() : null;
        Long customerId = customer.getId() != null ? customer.getId() : userId;

        return CustomerDTO.builder()
                .idCustomer(customerId)
                .idUser(userId)
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .age(user.getAge())
                .avatarUrl(user.getAvatarUrl())
                .isOnline(user.getIsOnline())
                .googleId(user.getGoogleId())
                .role(customer.getRole())
                .status(customer.getStatus())
                .dateOfBirth(customer.getDateOfBirth())
                .qrCodeUrl(customer.getQrCodeUrl())
                .lastBooking(customer.getLastBooking())
                .build();
    }

    public HostDTO toHostDTO(Host host) {
        User user = host.getUser();

        Long userId = user != null ? user.getId() : null;
        Long hostId = host.getId() != null ? host.getId() : userId;

        return HostDTO.builder()
                .idHost(hostId)
                .idUser(userId)
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .age(user.getAge())
                .avatarUrl(user.getAvatarUrl())
                .isOnline(user.getIsOnline())
                .googleId(user.getGoogleId())
                .role(host.getRole())
                .businessName(host.getBusinessName())
                .qrCodeUrl(host.getQrCodeUrl())
                .statusHost(host.getStatusHost())
                .build();
    }

    public AdminDTO toAdminDTO(Admin admin) {
        User user = admin.getUser();

        return AdminDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .age(user.getAge())
                .avatarUrl(user.getAvatarUrl())
                .isOnline(user.getIsOnline())
                .googleId(user.getGoogleId())
                .role(admin.getRole())
                .levelAdmin(admin.getLevelAdmin())
                .status(admin.getStatus())
                .build();
    }
}
