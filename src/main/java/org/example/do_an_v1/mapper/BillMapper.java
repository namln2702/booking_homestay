package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.HomestayDailyPricesDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.BillHomestayDailyPrice;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.Host;
import org.example.do_an_v1.entity.User;

import java.util.stream.Collectors;

public class BillMapper {

    /**
     * Chuyển từ Entity Bill sang DTO BillDTO
     */
    public static BillDTO toDTO(Bill bill) {
        if (bill == null) {
            return null;
        }

        return BillDTO.builder()
                .id(bill.getId())
                .code(bill.getCode())
                .status(bill.getStatus())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .checkIn(bill.getCheckIn())
                .checkOut(bill.getCheckOut())
                .actualCheckin(bill.getActualCheckinTime())
                .actualCheckout(bill.getActualCheckoutTime())
                .totalAmount(bill.getTotalAmount())
                .commission(bill.getCommission())

                // Thông tin khách hàng
                .customerDTO(CustomerMapper.toDTO(bill.getCustomer()))

                // Thông tin người đặt (booking info)
                .customerBookingInfoDTO(CustomerBookingInfoMapper.toDTO(bill.getCustomerBookingInfo()))

                // Thông tin homestay
                .homestayDTO(toHomestaySummary(bill.getHomestay()))

                // Thông tin địa chỉ homestay
                .addressDTO(bill.getHomestay() != null && bill.getHomestay().getAddress() != null
                        ? AddressMapper.toDTO(bill.getHomestay().getAddress())
                        : null)

                // Danh sách giá theo ngày - lấy từ BillHomestayDailyPrice
                .homestayDailyPricesDTOS(bill.getListBillHomestayDailyPrices() != null
                        ? bill.getListBillHomestayDailyPrices().stream()
                        .map(BillMapper::toHomestayDailyPricesDTO)
                        .collect(Collectors.toList())
                        : null)

                // Danh sách giao dịch
                .transactions(bill.getListTransaction() != null
                        ? bill.getListTransaction().stream()
                        .map(TransactionMapper::toDTO)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    /**
     * Chuyển từ DTO BillDTO sang Entity Bill (dùng khi tạo hoặc cập nhật)
     */
    public static Bill toEntity(BillDTO dto) {
        if (dto == null) {
            return null;
        }

        Bill bill = new Bill();

        bill.setId(dto.getId());
        bill.setCode(dto.getCode());
        bill.setStatus(dto.getStatus());
        bill.setCreatedAt(dto.getCreatedAt());
        bill.setUpdatedAt(dto.getUpdatedAt());
        bill.setCheckIn(dto.getCheckIn());
        bill.setCheckOut(dto.getCheckOut());
        bill.setActualCheckinTime(dto.getActualCheckin());
        bill.setActualCheckoutTime(dto.getActualCheckout());
        bill.setTotalAmount(dto.getTotalAmount());
        bill.setCommission(dto.getCommission());

        // Các quan hệ bên dưới sẽ được gán ở tầng Service để tránh lỗi detached entity
        // (vì cần lấy từ DB trước rồi set vào Bill)
        // Ví dụ:
        // bill.setCustomer(customerRepository.findById(dto.getCustomerDTO().getId()).get());

        return bill;
    }

    private static HomestaySummaryDTO toHomestaySummary(Homestay homestay) {
        if (homestay == null) {
            return null;
        }

        Host host = homestay.getHost();
        User hostUser = host != null ? host.getUser() : null;

        return HomestaySummaryDTO.builder()
                .id(homestay.getId())
                .title(homestay.getTitle())
                .category(homestay.getCategory())
                .status(homestay.getStatusHomestay())
                .hostId(host != null ? host.getId() : null)
                .hostName(hostUser != null ? hostUser.getName() : null)
                .city(homestay.getAddress() != null ? homestay.getAddress().getCity() : null)
                .state(homestay.getAddress() != null ? homestay.getAddress().getState() : null)
                .createdAt(homestay.getCreatedAt())
                .build();
    }

    /**
     * Map BillHomestayDailyPrice entity sang HomestayDailyPricesDTO
     * Lấy day và price từ BillHomestayDailyPrice
     */
    private static HomestayDailyPricesDTO toHomestayDailyPricesDTO(BillHomestayDailyPrice entity) {
        if (entity == null) {
            return null;
        }
        return HomestayDailyPricesDTO.builder()
                .id(entity.getHomestayDailyPrice() != null ? entity.getHomestayDailyPrice().getId() : null)
                .day(entity.getDay())
                .price(entity.getPrice())
                .build();
    }
}
