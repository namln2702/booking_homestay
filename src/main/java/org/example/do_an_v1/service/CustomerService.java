package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.BookingDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.payload.ApiResponse;

public interface CustomerService {


    ApiResponse<CustomerDTO> upsertCustomerProfile(CustomerDTO customerDTO) throws RuntimeException;
    ApiResponse<CustomerDTO> getCustomerByUserId(Long userId);
    ApiResponse<?> booking(Long userId, BookingDTO bookingDTO);

    ApiResponse<CustomerDTO> updatePreferencesCustomer(Long userId, CustomerDTO customerDTO);

    ApiResponse<?> reviewHomestay(Long userId, ReviewDTO reviewDTO);

    ApiResponse<?> updateReviewHomestay(Long userId, Long reviewId, ReviewDTO reviewDTO);

    /**
     * Thống kê các bill đã đặt của Customer
     */
    ApiResponse<?> getCustomerBills(Long userId);

    /**
     * Customer hủy bill
     * Nếu hủy trước 2 ngày so với check-in thì được hoàn tiền
     * Nếu muộn hơn thì không được hoàn tiền
     */
    ApiResponse<?> cancelBill(Long userId, Long billId);
}