package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.AdvancedHomestaySearchDTO;
import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HomestayDetailDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.dto.request.UpdateHomestayPriceRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface HomestayService {

    ApiResponse<HomestayDTO> createHomestay(Long hostUserId, HomestayCreateRequest request);

    ApiResponse<PageResponse<List<HomestayDTO>>> getHomestays(
            StatusHomestay status,
            int page,
            int size,
            Float minPrice,
            Float maxPrice,
            Integer minBedrooms,
            Integer minBathrooms,
            Integer minGuests,
            String city,
            String category,
            String search
    );

    ApiResponse<HomestayDTO> getHomestayDetailForAdmin(Long adminUserId, Long homestayId);

    ApiResponse<?> findHomestay(FindHomeStayDTO findHomeStayDTO);

    ApiResponse<?> searchHomestayAdvanced(AdvancedHomestaySearchDTO request);


    ApiResponse<?> detailHomestay(Long id);

    ApiResponse<HomestayDetailDTO> detailHomestayFull(Long id);
//
//    ApiResponse<?> reviewHomestay(Long userId, ReviewDTO reviewDTO);
//
//    ApiResponse<?> updateReviewHomestay(Long userId, Long reviewId, ReviewDTO reviewDTO);
//
//    ApiResponse<HomestayDTO> updateHomestayPrices(Long homestayId, UpdateHomestayPriceRequest request);
}
