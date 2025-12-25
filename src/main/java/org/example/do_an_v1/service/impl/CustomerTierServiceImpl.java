package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.CustomerTierDTO;
import org.example.do_an_v1.entity.Customer;
import org.example.do_an_v1.enums.CustomerTier;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.CustomerRepository;
import org.example.do_an_v1.repository.projection.CustomerTierStats;
import org.example.do_an_v1.service.CustomerTierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerTierServiceImpl implements CustomerTierService {

    private static final double REFUNDED_WEIGHT = 0.4d;
    private static final double GOLD_THRESHOLD = 5d;
    private static final double DIAMOND_THRESHOLD = 15d;

    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerTierDTO> getCustomerTier(Long customerId) {
        if (customerId == null) {
            return new ApiResponse<>(400, "Customer id is required", null);
        }

        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return new ApiResponse<>(404, "Customer not found with id: " + customerId, null);
        }

        CustomerTierStats stats = billRepository.findTierStatsByCustomerId(customerId);
        int successfulBookings = (stats != null && stats.getSuccessfulBookings() != null)
                ? stats.getSuccessfulBookings().intValue() : 0;
        int refundedBookings = (stats != null && stats.getRefundedBookings() != null)
                ? stats.getRefundedBookings().intValue() : 0;

        double effectiveOrders = calculateEffectiveOrders(successfulBookings, refundedBookings);
        CustomerTier tier = resolveTier(effectiveOrders);

        CustomerTierDTO dto = CustomerTierDTO.builder()
                .userId(customerId)
                .tier(tier)
                .successfulBookings(successfulBookings)
                .refundedBookings(refundedBookings)
                .effectiveOrders(effectiveOrders)
                .build();

        return new ApiResponse<>(200, "Customer tier retrieved", dto);
    }

    private double calculateEffectiveOrders(int successfulBookings, int refundedBookings) {
        return successfulBookings + refundedBookings * REFUNDED_WEIGHT;
    }

    private CustomerTier resolveTier(double effectiveOrders) {
        if (effectiveOrders >= DIAMOND_THRESHOLD) {
            return CustomerTier.DIAMOND;
        }
        if (effectiveOrders >= GOLD_THRESHOLD) {
            return CustomerTier.GOLD;
        }
        return CustomerTier.SILVER;
    }
}
