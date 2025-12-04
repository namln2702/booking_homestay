package org.example.do_an_v1.mapper.profile;

import org.example.do_an_v1.dto.AdminDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {

    public CustomerDTO toCustomerDTO(Customer customer) {
        User user = customer.getUser();

        Long userId = user != null ? user.getId() : null;
        Long customerId = customer.getId() != null ? customer.getId() : userId;

        // Map listPreference từ Set<Preference> sang List<Long>
        List<Long> listPreference = null;
        if (customer.getListPreferences() != null && !customer.getListPreferences().isEmpty()) {
            listPreference = customer.getListPreferences().stream()
                    .map(preference -> preference != null ? preference.getId() : null)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
        }

        return CustomerDTO.builder()
                .idCustomer(customerId)
                .idUser(userId)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .name(user != null ? user.getName() : null)
                .age(user != null ? user.getAge() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .isOnline(user != null ? user.getIsOnline() : null)
                .googleId(user != null ? user.getGoogleId() : null)
                .role(customer.getRole())
                .status(customer.getStatus())
                .dateOfBirth(customer.getDateOfBirth())
                .qrCodeUrl(customer.getQrCodeUrl())
                .lastBooking(customer.getLastBooking())
                .listPreference(listPreference)
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

        Long userId = user != null ? user.getId() : null;

        return AdminDTO.builder()
                .idAdmin(admin.getId())
                .idUser(userId)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .name(user != null ? user.getName() : null)
                .age(user != null ? user.getAge() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .isOnline(user != null ? user.getIsOnline() : null)
                .googleId(user != null ? user.getGoogleId() : null)
                .role(admin.getRole())
                .levelAdmin(admin.getLevelAdmin())
                .status(admin.getStatus())
                .build();
    }
}
