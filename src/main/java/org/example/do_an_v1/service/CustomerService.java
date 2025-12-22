package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.BookingDTO;
import org.example.do_an_v1.dto.ComplaintDTO;
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
    ApiResponse<?> getCustomerOrders(Long userId);
    ApiResponse<?> getCustomerOrderDetail(Long userId, Long billId);
    ApiResponse<?> getCustomerComplaints(Long userId);

    /**
     * Customer hủy bill
     * Nếu hủy trước 2 ngày so với check-in thì được hoàn tiền
     * Nếu muộn hơn thì không được hoàn tiền
     */
    ApiResponse<?> cancelBill(Long userId, Long billId);

    /**
     * Customer tạo khiếu nại
     * Chỉ có thể khiếu nại trong thời gian (N + 1) ngày sau checkout
     * N là số ngày đặt phòng (từ checkIn đến checkOut)
     */
    ApiResponse<?> createComplaint(Long userId, ComplaintDTO complaintDTO);

    /**
     * Customer cập nhật khiếu nại
     * Chỉ có thể update khi complaint chưa được xử lý bởi admin
     */
    ApiResponse<?> updateComplaint(Long userId, Long complaintId, ComplaintDTO complaintDTO);
}
