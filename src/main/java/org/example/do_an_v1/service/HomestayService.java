package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface HomestayService {

    ApiResponse<HomestayDTO> createHomestay(Long hostUserId, HomestayCreateRequest request);

    ApiResponse<PageResponse<List<HomestaySummaryDTO>>> getHomestays(StatusHomestay status,
                                                                     int page,
                                                                     int size);

    ApiResponse<HomestayDTO> getHomestayDetailForAdmin(Long adminUserId, Long homestayId);

    ApiResponse<HomestayDTO> approveHomestay(Long adminUserId, Long homestayId);

    ApiResponse<?> findHomestay(FindHomeStayDTO findHomeStayDTO);

//    ApiResponse<?> getAll(int page, int size);
    ApiResponse<?> findUserHistoryHomestays(Long userId);

    ApiResponse<?> detailHomestay(Long id);

    ApiResponse<?> reviewHomestay(Long userId, ReviewDTO reviewDTO);

    ApiResponse<?> updateReviewHomestay(Long userId, Long reviewId, ReviewDTO reviewDTO);
}
