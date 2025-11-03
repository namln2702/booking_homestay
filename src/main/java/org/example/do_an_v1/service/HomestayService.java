package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HomestaySummaryDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.dto.response.PageResponse;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.payload.ApiResponse;

import java.util.List;

public interface HomestayService {

    ApiResponse<HomestayDTO> createHomestay(Long hostUserId, HomestayCreateRequest request);

    ApiResponse<PageResponse<List<HomestaySummaryDTO>>> getHomestaysForAdmin(Long adminUserId,
                                                                            StatusHomestay status,
                                                                            int page,
                                                                            int size);

    ApiResponse<HomestayDTO> getHomestayDetailForAdmin(Long adminUserId, Long homestayId);

    ApiResponse<HomestayDTO> approveHomestay(Long adminUserId, Long homestayId);
}
