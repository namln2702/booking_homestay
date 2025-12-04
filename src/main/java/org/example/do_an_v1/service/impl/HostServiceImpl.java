package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.request.CheckinRequest;
import org.example.do_an_v1.dto.request.HostRegistrationRequest;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.entity.Admin;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.enums.StatusHost;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.enums.StatusBill;
import org.example.do_an_v1.mapper.BillMapper;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.HomestayRepository;
import org.example.do_an_v1.repository.HostRepository;
import org.example.do_an_v1.service.HostService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {

    private final HostRepository hostRepository;
    private final AdminRepository adminRepository;
    private final ProfileMapper profileMapper;
    private final UserRegistrationSupport userRegistrationSupport;
    private final BillRepository billRepository;
    private final HomestayRepository homestayRepository;

    @Override
    @Transactional
    public ApiResponse<HostDTO> registerHost(Long userId, HostRegistrationRequest request) throws RuntimeException {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required to register as host");
        }

        if (request == null) {
            throw new IllegalArgumentException("Host registration request is required");
        }

        User user = userRegistrationSupport.getUserOrThrow(userId);

        Host existingHost = hostRepository.findById(user.getId()).orElse(null);
        if (existingHost != null) {
            return new ApiResponse<>(409, "Host profile already exists for this user", profileMapper.toHostDTO(existingHost));
        }

        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                userId,
                request.getUsername(),
                request.getName(),
                request.getPhone(),
                request.getAge(),
                request.getAvatarUrl()
        );

        userRegistrationSupport.applyUserAttributes(user, userRequest);

        Host host = Host.builder()
                .user(user)
                .role(RoleUser.HOST)
                .statusHost(StatusHost.PENDING)
                .businessName(request.getBusinessName())
                .qrCodeUrl(request.getQrCodeUrl())
                .build();

        Host savedHost = hostRepository.save(host);

        return new ApiResponse<>(201, "Host profile created successfully", profileMapper.toHostDTO(savedHost));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<List<HostDTO>>> getHostsForAdmin(Long adminUserId, StatusHost status, int page, int size) {
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Host> hostPage = status != null
                ? hostRepository.findByStatusHost(status, pageable)
                : hostRepository.findAll(pageable);

        List<HostDTO> hosts = hostPage.getContent().stream()
                .map(profileMapper::toHostDTO)
                .toList();

        PageResponse<List<HostDTO>> response = PageResponse.<List<HostDTO>>builder()
                .page(hostPage.getNumber())
                .size(hostPage.getSize())
                .total(hostPage.getTotalElements())
                .items(hosts)
                .build();

        return new ApiResponse<>(200, "Hosts retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HostDTO> getHostDetailForAdmin(Long adminUserId, Long hostUserId) {
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        Host host = hostRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host profile not found for user id " + hostUserId));

        return new ApiResponse<>(200, "Host detail retrieved successfully", profileMapper.toHostDTO(host));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HostDTO> getHostByUserId(Long userId) {
        Host host = hostRepository.findById(userId).orElse(null);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found", null);
        }
        return new ApiResponse<>(200, "Host profile retrieved successfully", profileMapper.toHostDTO(host));
    }

    @Override
    @Transactional
    public ApiResponse<HostDTO> approveHost(Long adminUserId, Long hostUserId) {
        Admin admin = requireActiveAdmin(adminUserId);
        if (admin == null) {
            return new ApiResponse<>(403, "Admin account is not active", null);
        }

        Host host = hostRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host profile not found for user id " + hostUserId));

        if (host.getStatusHost() == StatusHost.ACTIVE) {
            return new ApiResponse<>(409, "Host has already been approved", profileMapper.toHostDTO(host));
        }

        if (host.getStatusHost() != StatusHost.PENDING) {
            return new ApiResponse<>(409, "Host status must be pending before approval", profileMapper.toHostDTO(host));
        }

        host.setStatusHost(StatusHost.ACTIVE);
        if (host.getRole() != RoleUser.HOST) {
            host.setRole(RoleUser.HOST);
        }
        Host savedHost = hostRepository.save(host);
        return new ApiResponse<>(200, "Host approved successfully", profileMapper.toHostDTO(savedHost));
    }

    private Admin requireActiveAdmin(Long adminUserId) {
        if (adminUserId == null) {
            throw new IllegalArgumentException("Admin user id is required");
        }
        Admin admin = adminRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for user id " + adminUserId));
        if (!Objects.equals(admin.getStatus(), Status.ACTIVE)) {
            return null;
        }
        return admin;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getHomestaysForHost(Long hostUserId) {
        if (hostUserId == null) {
            throw new IllegalArgumentException("Host user id is required");
        }

        User user = userRegistrationSupport.getUserOrThrow(hostUserId);
        Host host = hostRepository.findByUser(user);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found for this user", null);
        }

        List<Homestay> homestays = homestayRepository.findByHost(host);

        List<HomestaySummaryDTO> summaries = homestays.stream()
                .map(h -> HomestaySummaryDTO.builder()
                        .id(h.getId())
                        .title(h.getTitle())
                        .category(h.getCategory())
                        .status(h.getStatusHomestay())
                        .hostId(host.getId())
                        .hostName(user.getName())
                        .city(h.getAddress() != null ? h.getAddress().getCity() : null)
                        .state(h.getAddress() != null ? h.getAddress().getState() : null)
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();

        return new ApiResponse<>(200, "Homestays for host retrieved successfully", summaries);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getBillsForHostHomestays(Long hostUserId) {
        if (hostUserId == null) {
            throw new IllegalArgumentException("Host user id is required");
        }

        User user = userRegistrationSupport.getUserOrThrow(hostUserId);
        Host host = hostRepository.findByUser(user);
        if (host == null) {
            return new ApiResponse<>(404, "Host profile not found for this user", null);
        }

        List<Bill> bills = billRepository.findByHomestay_Host(host);

        // Lọc các bill đã/đang được sử dụng (logic tương tự history customer)
        List<Bill> filtered = bills.stream()
                .filter(bill -> {
                    StatusBill status = bill.getStatus();
                    return status == StatusBill.SUCCEED
//                            || status == StatusBill.COMPLAINT_EXPIRED
                            || status == StatusBill.CHECKIN_EXPIRED
                            || status == StatusBill.COMPLAINT_PENDING
                            || status == StatusBill.CHECKIN_PENDING;
                })
                .toList();

        List<BillDTO> billDTOS = filtered.stream()
                .map(BillMapper::toDTO)
                .toList();

        return new ApiResponse<>(200, "Bills for host homestays retrieved successfully", billDTOS);
    }

    @Override
    @Transactional
    public ApiResponse<?> confirmCheckin(Long hostUserId, CheckinRequest request) {
        if (hostUserId == null) {
            throw new IllegalArgumentException("Host user ID is required");
        }
        if (request == null || request.getBillId() == null) {
            throw new IllegalArgumentException("Bill ID is required");
        }

        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + request.getBillId()));

        // Validate: Bill phải có homestay
        if (bill.getHomestay() == null) {
            throw new IllegalStateException("Bill must have a homestay associated");
        }

        // Validate: Host phải sở hữu homestay này
        Long homestayHostId = bill.getHomestay().getHost().getId();
        if (!Objects.equals(homestayHostId, hostUserId)) {
            throw new IllegalStateException("Host can only check-in customers for their own homestays");
        }

        // Validate: Bill phải ở trạng thái CHECKIN_PENDING
        if (bill.getStatus() != StatusBill.CHECKIN_PENDING) {
            throw new IllegalStateException("Bill must be in CHECKIN_PENDING status to confirm checkin. Current status: " + bill.getStatus());
        }

        // Cập nhật trạng thái bill thành COMPLAINT_PENDING
        bill.setStatus(StatusBill.COMPLAINT_PENDING);
        bill.setActualCheckinTime(LocalDateTime.now());
        billRepository.save(bill);

        return new ApiResponse<>(200, "Check-in confirmed successfully. Bill status changed to COMPLAINT_PENDING", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> confirmCheckout(org.example.do_an_v1.dto.request.CheckoutRequest request) {
        if (request == null || request.getBillId() == null) {
            throw new IllegalArgumentException("Bill ID is required");
        }

        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + request.getBillId()));

        // Validate: Bill phải ở trạng thái COMPLAINT_PENDING
        if (bill.getStatus() != StatusBill.COMPLAINT_PENDING) {
            throw new IllegalStateException("Bill must be in COMPLAINT_PENDING status to confirm checkout. Current status: " + bill.getStatus());
        }

        // Validate: Host phải sở hữu homestay này
        // (Có thể thêm validation này nếu cần)

        // Cập nhật trạng thái bill thành SUCCEED
        bill.setStatus(StatusBill.SUCCEED);
        billRepository.save(bill);

        return new ApiResponse<>(200, "Check-out confirmed successfully. Bill status changed to SUCCEED", null);
    }
}
